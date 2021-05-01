# SoftCodec 

[![Build](https://github.com/BruceWind/SoftCodec/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/BruceWind/SoftCodec/actions/workflows/build.yml)

- [x] 1. encode with x264.
- [x] 2. push into RTMP server.
- [ ] 3. encode with openh264.

## Building & Testing 

**1. building**

It depend on NDK 16, but you don't need to download manually. 
By the time you executed `./gradlew assembleDebug`, gradle will download it automatically in the event that 
your computer does not has NDK 16.
# 
**2. Testing with a RTMP server:**

You may not have a RTMP server.

You need to establish a RTMP server which receives app pushed RTMP stream , and transfers stream to 
other players.
 
I had written a blog to teach someone else how to establish it.
You can look into the [blog](https://github.com/BruceWind/BruceWind.github.io/blob/master/md/establish-RTMP-server-with-docker.md).

---------------

In addition, `master` branch has all of stable codes, If you want to look code in development, checkout
 other branches.
