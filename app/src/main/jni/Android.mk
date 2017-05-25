LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


OpenCV_INSTALL_MODULES := on
OpenCV_CAMERA_MODULES := off

OPENCV_LIB_TYPE :=STATIC

ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
include ..\..\..\..\native\jni\OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif

LOCAL_MODULE := OPEN_CV

LOCAL_SRC_FILES :=test.cpp

LOCAL_LDLIBS +=  -ljnigraphics -lGLESv1_CM -latomic	#-latomic 解决 undefined reference to '__atomic_fetch_add_4'   -ljnigraphics解决undefined reference to 'AndroidBitmap_getInfo'
include $(BUILD_SHARED_LIBRARY)