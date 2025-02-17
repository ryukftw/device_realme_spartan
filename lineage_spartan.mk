#
# Copyright (C) 2018 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from spartan device
$(call inherit-product, device/realme/spartan/device.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_NAME := lineage_spartan
PRODUCT_DEVICE := spartan
PRODUCT_MANUFACTURER := realme
PRODUCT_BRAND := realme
PRODUCT_MODEL := RMX3371

# Aperture Camera
PRODUCT_NO_CAMERA := true

PRODUCT_GMS_CLIENTID_BASE := android-oppo

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="RMX3371-user 14 UP1A.230620.001 S.14bef93_5a18-26df2 release-keys" \
    BuildFingerprint=realme/RMX3371/RE54E4L1:14/UP1A.230620.001/S.14bef93_5a18-26df2:user/release-keys \
    DeviceName=RE54E4L1 \
    DeviceProduct=RMX3371 \
    SystemDevice=RE54E4L1 \
    SystemName=RMX3371

