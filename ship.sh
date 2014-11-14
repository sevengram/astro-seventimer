#! /bin/bash

rm -rf build
if [ ! -d "dist" ]; then
  mkdir dist
fi

mkdir -p build
cp -r assets build
cp -r libs build
cp -r res build
cp -r src build
cp -r ant build
cp *.xml build
cp *.properties build
cp -r ../common/libs build
cp -r ../common/src build
cp -r keystore/deepskygroup.keystore build

cd build
rm -f libs/android.jar
for ((INSTALL_CHANNEL = 1; INSTALL_CHANNEL <= 9; ++INSTALL_CHANNEL));
do
	ant ship -Dapp.channel=$INSTALL_CHANNEL;
done
cd ..
mv build/dist/*.apk dist
rm -rf build
