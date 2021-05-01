# SoftCodec

- [x] 1.encode with x264.
- [x] 2. push into RTMP server.


## Building & Testing 

1. building

It depend on NDK 16, but you don't need to download manually. 
In the time, you run `./gradlew assembleDebug` gradle will make download process automatically in the event that 
your computer does not has NDK 16.



2.Testing with a RTMP server:

You need establish a RTMP server to receive the RTMP stream which the app pushed.

You may has not a RTMP server, I had written a blog to teach someone else how to establish it.

You can look into the [blog](https://github.com/BruceWind/BruceWind.github.io/blob/master/md/establish-RTMP-server-with-docker.md).

-------------------------

In addition, master branch has all of stable codes, If you want to look into develop code, checkout
 others.
