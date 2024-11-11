#
# Copyright (C) 2018-2024 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_DEVICE),spartan)
include $(call all-makefiles-under,$(LOCAL_PATH))

include $(CLEAR_VARS)

# A/B builds require us to create the mount points at compile time.
# Just creating it for all cases since it does not hurt.
FIRMWARE_MOUNT_POINT := $(TARGET_OUT_VENDOR)/firmware_mnt
$(FIRMWARE_MOUNT_POINT): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating $(FIRMWARE_MOUNT_POINT)"
	@mkdir -p $(TARGET_OUT_VENDOR)/firmware_mnt

BT_FIRMWARE_MOUNT_POINT := $(TARGET_OUT_VENDOR)/bt_firmware
$(BT_FIRMWARE_MOUNT_POINT): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating $(BT_FIRMWARE_MOUNT_POINT)"
	@mkdir -p $(TARGET_OUT_VENDOR)/bt_firmware

DSP_MOUNT_POINT := $(TARGET_OUT_VENDOR)/dsp
$(DSP_MOUNT_POINT): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating $(DSP_MOUNT_POINT)"
	@mkdir -p $(TARGET_OUT_VENDOR)/dsp

ALL_DEFAULT_INSTALLED_MODULES += $(FIRMWARE_MOUNT_POINT) $(BT_FIRMWARE_MOUNT_POINT) $(DSP_MOUNT_POINT)

CAMERA_COMPONENTS_SYMLINKS := $(TARGET_OUT_VENDOR)/lib64
$(CAMERA_COMPONENTS_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating camera components symlinks: $@"
	@mkdir -p $@
	@mkdir -p $@/camera/components
	$(hide) ln -sf /odm/lib64/camera/components/com.qti.stats.pdlib.so $@/camera/components/com.qti.stats.pdlib.so
	$(hide) ln -sf /odm/lib64/camera/components/com.qti.stats.haf.so $@/camera/components/com.qti.stats.haf.so
	$(hide) ln -sf /odm/lib64/camera/components/libipebpsstriping.so $@/libipebpsstriping.so

ALL_DEFAULT_INSTALLED_MODULES += $(CAMERA_COMPONENTS_SYMLINKS)
endif
