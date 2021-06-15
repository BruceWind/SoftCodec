# SoftCodec 

[![Build](https://github.com/BruceWind/SoftCodec/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/BruceWind/SoftCodec/actions/workflows/build.yml)

- [x] 1. x264编码集成.
- [x] 2. 推流到RTMP server.
- [x] 3. openh264编码集成.
- [ ] 4. 软件回声消除.
- [ ] 5. 美颜 or 提升亮度 or HDR.

## 该仓库做了什么工作:

``` javascrpt                                                     
         phone                                                          
+-----------------------+                                               
|                       |                                               
|                       |                                               
|     +-----------+     |                                               
|     |           |     |   +----------------+        +----------------+
|     |  Camera   |-------->|  YUV format A  | -----> |  YUV format B  |
|     |           |     |   +----------------+        +----------------+
|     +-----------+     |                                     |         
|                       |                                     |         
|                       |                                     |         
|                       |                                     v         
|                       |      +------------+         +---------------+ 
|                       |      |            |         |               | 
|                       |      | YUV(NALs)  |<------  |     Codec     | 
|                       |      |            |         |               | 
|                       |      +------------+         +---------------+ 
|                       |            |                                  
|                       |            |                                  
|                       |            |                                  
|                       |            |                                  
|                       |            v                                  
|    +-------------+    |     +-------------+                           
|    |             |    |     |             |                           
|    |     WIFI    |<-------- | RTMP & FLV  |                           
|    |             |    |     |             |                           
|    +-------------+    |     +-------------+                           
|           |           |                                               
+-----------|-----------+                                               
            |                                                           
            |                      RTMP server                                     
            |                    +-----------+                                  
            |                    |           |
            |                    |   _____   |            
            |                    |  |_____|  |            
            +------------------->|    ___    |
                                 |   |___|   |
                                 |           |
                                 |           |
                                 |           |
                                 +-----------+            
```


## Building & Testing 

**1. 编译**

目前gradle里配置了ndk版本是16，您无需手动配置下载NDK，运行编译命令时会自动执行，电脑无对应版本的ndk则会优先下载ndk，请注意网络环境。

**2.建立RTMP server:**

您可能还没有RTMP服务器. 那么您需要自己建立一个rtmp服务器用于收发数据流。
 
I had written a blog to teach someone else how to establish it.
我写了一篇blog教别人如何建立一个rtmp服务器：
这里是：[blog](https://github.com/BruceWind/BruceWind.github.io/blob/master/md/establish-RTMP-server-with-docker.md).

**3. 测试推流**

修改`MainActivity`中如下代码:
``` java 
private String mRtmpPushUrl = "rtmp://192.168.50.14/live/live";
```
---------------
需要补充的是:
 1. `master` 分支是稳定版本的代码，若您需要开发中的代码，请移步其他分支。感谢您的star。
 2. 该仓库的开源协议是[GPL](https://github.com/BruceWind/SoftCodec/blob/master/LINCENSE_CN)，劳烦您遵守此协议。
