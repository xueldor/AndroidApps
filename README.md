自己开发的app，学习目的。有一些是系统级别的，需要平台签名或内置到系统。

* AppPeace：冻结应用，类似网上一个很有名的叫做icebox或小黑屋的应用。安装后，如果使用hide模式，需要把应用设置为device owner；如果使用disable模式，需要你的uid是system，并打上系统platform签名。

* AutoShutdown: AOSP没有自动关机功能，于是自己写的一个应用。需要platform签名。目前测下来有时会比设定时间晚两分钟。
* XueNLP: AOSP提供了网络定位接口，但是内部没有实现，因为这个功能是需要第三方提供服务的。谷歌自己提供了网络定位服务因为网络原因国内用不了。国内一般用百度或高德的服务。这是我用BaiduNetworkLocationProvider实现的，把它集成到`/system/app`即可。