echo 'Cloning vendor'
git clone https://github.com/ryukftw/vendor_realme_spartan.git -b 14 vendor/realme/spartan

echo "Cloning kernel"
git clone https://github.com/ryukftw/kernel_realme_sm8250.git -b groot kernel/realme/sm8250 --depth=1

echo "Cloning oplus hals"
git clone https://github.com/ryukftw/android_hardware_oplus.git -b 14 hardware/oplus

echo 'Cloning Oplus Camera'
git clone https://gitlab.com/ryukftw/proprietary_vendor_oplus_camera.git -b lineage-21 vendor/oplus/camera
