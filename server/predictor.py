"""
Port logic inference từ FloodPredictionHelper.kt sang Python.
Dùng Keras model (.h5) thay vì TFLite để tránh vấn đề Flex delegate.
Dùng scalers.pkl (StandardScaler thật) thay vì hardcode mean/scale.
"""

import math
import numpy as np
import joblib
from datetime import datetime, timedelta

CM_TO_FEET = 0.0328084
FEET_TO_CM = 30.48

def _load_keras_compat(model_path: str):
    """
    Bypass Keras serialization hoàn toàn.
    - Rebuild model từ kiến trúc đã biết (không có GaussianNoise)
    - Đọc weights trực tiếp từ h5py groups theo tên layer
    
    Layer names lấy từ config trong file h5:
      lstm_1, batch_normalization_1, dropout_1, dense_1
    """
    import tensorflow as tf
    import h5py
    import numpy as np

    # ── Rebuild model (không có GaussianNoise) ───────────────────────────
    # Tên layer phải khớp với tên trong file .h5 để load weights đúng
    model = tf.keras.Sequential([
        tf.keras.layers.LSTM(
            units=6, return_sequences=False,
            kernel_regularizer=tf.keras.regularizers.l2(0.05),
            name="lstm_1",
        ),
        tf.keras.layers.BatchNormalization(
            axis=1, momentum=0.99, epsilon=0.001,
            name="batch_normalization_1",
        ),
        tf.keras.layers.Dropout(0.7, name="dropout_1"),
        tf.keras.layers.Dense(1, activation="linear", name="dense_1"),
    ], name="sequential_server")

    # Khởi tạo weights bằng 1 forward pass
    model(np.zeros((1, 24, 5), dtype=np.float32), training=False)

    # ── Đọc weights từ h5py ──────────────────────────────────────────────
    def read_layer(mw: h5py.Group, layer_name: str) -> dict:
        """Đọc weight dict từ model_weights/{layer_name}."""
        if layer_name not in mw:
            print(f"  [WARN] Layer '{layer_name}' không có trong h5.")
            return {}
        lg = mw[layer_name]
        # Keras 2 lưu 2 cấp: layer_name/layer_name/var:0
        vg = lg[layer_name] if layer_name in lg else lg
        return {k: np.array(vg[k]) for k in vg.keys()
                if isinstance(vg[k], h5py.Dataset)}

    with h5py.File(model_path, "r") as f:
        if "model_weights" not in f:
            raise ValueError("Không tìm thấy 'model_weights' trong file .h5")
        mw = f["model_weights"]

        # LSTM — thứ tự: kernel, recurrent_kernel, bias
        w = read_layer(mw, "lstm_1")
        if w:
            model.get_layer("lstm_1").set_weights([
                w["kernel:0"],
                w["recurrent_kernel:0"],
                w["bias:0"],
            ])
            print("[Predictor] ✅ LSTM weights loaded")

        # BatchNorm — thứ tự: gamma, beta, moving_mean, moving_variance
        w = read_layer(mw, "batch_normalization_1")
        if w:
            model.get_layer("batch_normalization_1").set_weights([
                w["gamma:0"],
                w["beta:0"],
                w["moving_mean:0"],
                w["moving_variance:0"],
            ])
            print("[Predictor] ✅ BatchNorm weights loaded")

        # Dense — thứ tự: kernel, bias
        w = read_layer(mw, "dense_1")
        if w:
            model.get_layer("dense_1").set_weights([
                w["kernel:0"],
                w["bias:0"],
            ])
            print("[Predictor] ✅ Dense weights loaded")

    return model


class FloodPredictor:
    def __init__(self, model_path: str, scalers_path: str):
        """
        Args:
            model_path: Đường dẫn tới flood_model_server.h5 (Keras model)
            scalers_path: Đường dẫn tới scalers.pkl
        """
        import tensorflow as tf

        # ── Load scalers ──────────────────────────────────────────────────
        scalers = joblib.load(scalers_path)
        self._feat_scaler = scalers["feat"]
        self._targ_scaler = scalers["targ"]
        print(f"[Predictor] Scalers loaded. feat.mean_: {self._feat_scaler.mean_}")

        # ── Load Keras model ──────────────────────────────────────────────
        # training=False → GaussianNoise và Dropout không hoạt động
        self._model = _load_keras_compat(model_path)
        self._model.trainable = False
        print(f"[Predictor] Keras model loaded. Params: {self._model.count_params()}")

    # ── Private helpers ────────────────────────────────────────────────────

    def _preprocess(self, historical_24: list[dict]) -> np.ndarray:
        """
        Convert 24 HourlyData dicts → numpy array shape (1, 24, 5) đã chuẩn hóa.
        Unit: cm → feet (giống FloodPredictionHelper.kt)
        """
        rows = []
        for h in historical_24:
            rainfall_ft    = h["rainfall_cm"]    * CM_TO_FEET
            water_level_ft = h["water_level_cm"] * CM_TO_FEET
            rows.append([
                rainfall_ft,
                h["water_volume"],
                h["hr_sin"],
                h["hr_cos"],
                water_level_ft,
            ])
        rows_np = np.array(rows, dtype=np.float32)           # (24, 5)
        scaled  = self._feat_scaler.transform(rows_np)       # (24, 5)
        return scaled.reshape(1, 24, 5).astype(np.float32)   # (1, 24, 5)

    def _postprocess(self, raw_output: float) -> float:
        """Inverse-transform output → cm."""
        water_level_ft = self._targ_scaler.inverse_transform([[raw_output]])[0][0]
        return max(0.0, float(water_level_ft) * FEET_TO_CM)

    def _infer_one_step(self, historical_24: list[dict]) -> float:
        """Chạy 1 bước inference qua Keras, trả về mực nước (cm)."""
        input_data = self._preprocess(historical_24)
        # training=False → GaussianNoise và Dropout không hoạt động
        raw = self._model(input_data, training=False)[0][0].numpy()
        return self._postprocess(float(raw))

    # ── Public API ─────────────────────────────────────────────────────────

    def predict_steps(self, historical_24: list[dict],
                      last_timestamp_ms: int, num_steps: int) -> list[float]:
        """
        Dự báo num_steps bước (mỗi bước = 5 phút) từ cửa sổ 24 timestep.

        FIX 1: jump_offset áp dụng ngay trong vòng lặp để tránh cascade collapse.
        FIX 2: Rate dampening — giới hạn tốc độ thay đổi theo trend lịch sử,
               ngăn autoregressive drift làm predictions tăng/giảm phi thực tế.
        """
        current_list  = list(historical_24)
        predictions   = []
        start_dt      = datetime.fromtimestamp(last_timestamp_ms / 1000)
        current_level = historical_24[-1]["water_level_cm"]
        jump_offset   = None

        # ── Tính tốc độ thay đổi thực tế từ 24 điểm lịch sử ────────────────
        # Dùng 10 điểm gần nhất để ước lượng tốc độ ổn định hơn
        recent = [h["water_level_cm"] for h in historical_24[-10:]]
        if len(recent) >= 2:
            observed_rate = (recent[-1] - recent[0]) / (len(recent) - 1)  # cm/step
        else:
            observed_rate = 0.0

        # Giới hạn tốc độ thay đổi cho phép:
        # - Tối thiểu: 1.5 cm/bước (5 phút) = 18 cm/giờ
        # - Tối đa: 5 cm/bước (5 phút) = 60 cm/giờ (lũ cực mạnh)
        # - Cho phép 3× tốc độ quan sát thực tế, nhưng trong khoảng [1.5, 5.0]
        max_delta = max(1.5, min(5.0, abs(observed_rate) * 3 + 1.0))
        print(f"[Predictor] observed_rate={observed_rate:.2f} cm/step, "
              f"max_delta={max_delta:.2f} cm/step")

        prev_corrected = current_level  # dùng để dampen

        for step_idx in range(num_steps):
            next_cm_raw = self._infer_one_step(current_list[-24:])

            # Tính jump_offset ở bước đầu để căn chỉnh điểm bắt đầu
            if step_idx == 0:
                jump_offset = next_cm_raw - current_level

            # Apply offset correction
            next_cm_unclamped = next_cm_raw - jump_offset

            # ── Rate dampening: không cho phép thay đổi quá max_delta/bước ──
            delta = next_cm_unclamped - prev_corrected
            if delta > max_delta:
                next_cm_unclamped = prev_corrected + max_delta
            elif delta < -max_delta:
                next_cm_unclamped = prev_corrected - max_delta

            next_cm = max(0.0, next_cm_unclamped)
            predictions.append(next_cm)
            prev_corrected = next_cm  # track giá trị đã corrected

            start_dt     = start_dt + timedelta(minutes=5)
            decimal_hour = start_dt.hour + start_dt.minute / 60.0

            last_rain   = current_list[-1]["rainfall_cm"]
            future_rain = last_rain * 0.5 if last_rain * 0.5 >= 0.1 else 0.0

            # Feed back giá trị đã correct + dampen để tránh drift
            current_list.append({
                "rainfall_cm":    future_rain,
                "water_volume":   next_cm * 10.0,
                "hr_sin":         math.sin(2 * math.pi * decimal_hour / 24),
                "hr_cos":         math.cos(2 * math.pi * decimal_hour / 24),
                "water_level_cm": next_cm,
            })

        return predictions

