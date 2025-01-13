#
# Copyright (C) 2018-2024 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_DEVICE),spartan)
include $(call all-makefiles-under,$(LOCAL_PATH))

include $(CLEAR_VARS)

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
