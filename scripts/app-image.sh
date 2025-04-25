#!/bin/bash
set -x
# compile the Java source files
javac --module-path src/main/java/com/kingjoe/orion -d out $(find src/main -name "*.java")

# create the application image for macOS-arch64
mkdir -p jdk/extracted
cd jdk/extracted || exit 1
tar -xf ../jdk-23.0.2_macos-aarch64_bin.tar
cd -
jlink --module-path jdk/extracted/jdk-23.0.2.jdk/jmods:out --add-modules com.kingjoe.orion --launcher orion=com.kingjoe.orion/com.kingjoe.orion.jrion.Rion --output orion-runtime/mac-aarch64/orion
rm -rf jdk/extracted/jdk-23.0.2.jdk

# create the application image for macOS-x64
cd jdk/extracted || exit 1
tar -xf ../jdk-23.0.2_macos-x64_bin.tar
cd -
jlink --module-path jdk/extracted/jdk-23.0.2.jdk/jmods:out --add-modules com.kingjoe.orion --launcher orion=com.kingjoe.orion/com.kingjoe.orion.jrion.Rion --output orion-runtime/mac-x64/orion
rm -rf jdk/extracted/jdk-23.0.2.jdk

# update the launcher script for macOS
cat << "EOF" - > orion-runtime/mac-aarch64/orion/bin/orion
#!/bin/sh
JLINK_VM_OPTIONS=
DIR=$(dirname "$(readlink -f "$0")")
$DIR/java $JLINK_VM_OPTIONS -m com.kingjoe.orion/com.kingjoe.orion.jrion.Rion "$@"
EOF

cat << "EOF" - > orion-runtime/mac-x64/orion/bin/orion
#!/bin/sh
JLINK_VM_OPTIONS=
DIR=$(dirname "$(readlink -f "$0")")
$DIR/java $JLINK_VM_OPTIONS -m com.kingjoe.orion/com.kingjoe.orion.jrion.Rion "$@"
EOF


# create the application image for Linux aarch64
cd jdk/extracted || exit 1
tar -xf ../jdk-23.0.2_linux-aarch64_bin.tar
cd -
jlink --module-path jdk/extracted/jdk-23.0.2/jmods:out --add-modules com.kingjoe.orion --launcher orion=com.kingjoe.orion/com.kingjoe.orion.jrion.Rion --output orion-runtime/linux-aarch64/orion
rm -rf jdk/extracted/jdk-23.0.2

# create the application image for Linux x64
cd jdk/extracted || exit 1
tar -xf ../jdk-23.0.2_linux-x64_bin.tar
cd -
jlink --module-path jdk/extracted/jdk-23.0.2/jmods:out --add-modules com.kingjoe.orion --launcher orion=com.kingjoe.orion/com.kingjoe.orion.jrion.Rion --output orion-runtime/linux-x64/orion
rm -rf jdk/extracted/jdk-23.0.2

# create the application image for Windows
cd jdk/extracted || exit 1
unzip -o ../jdk-23.0.2_windows-x64_bin.zip
cd -
jlink --module-path jdk/extracted/jdk-23.0.2/jmods:out --add-modules com.kingjoe.orion --launcher orion=com.kingjoe.orion/com.kingjoe.orion.jrion.Rion --output orion-runtime/windows/orion
rm -rf jdk/extracted/jdk-23.0.2

# copy uninstall script to the macOS images
cp scripts/mac/uninstall_orion.sh orion-runtime/mac-aarch64/orion/
cp scripts/mac/uninstall_orion.sh orion-runtime/mac-x64/orion/

mkdir -p binaries
# create pkg for macOS-aarch64
pkgbuild --root orion-runtime/mac-aarch64/orion --identifier com.kingjoe.orion --version 1.0.0 --install-location /usr/local/orion --scripts scripts/mac binaries/orion-mac-aarch64.pkg

# create pkg for macOS-x64
pkgbuild --root orion-runtime/mac-x64/orion --identifier com.kingjoe.orion --version 1.0.0 --install-location /usr/local/orion --scripts scripts/mac binaries/orion-mac-x64.pkg

# create tar.gz for Linux aarch64
cd ./orion-runtime/linux-aarch64 || exit 1
tar -czvf ../../binaries/orion-linux-aarch64.tar.gz orion

# create tar.gz for Linux x64
cd ../linux-x64 || exit 1
tar -czvf ../../binaries/orion-linux-x64.tar.gz orion

# create zip for Windows
cd ../windows || exit 1
zip -r ../../binaries/orion-win.zip orion
