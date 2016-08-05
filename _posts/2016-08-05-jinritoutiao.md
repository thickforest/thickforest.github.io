---
layout: post
title:  "今日头条!"
date:   2016-08-05 17:01:42 +0800
categories: 今日头条
---
!(http://p1.pstatp.com/large/ac800073da9b5550968)

自从将公司电脑的Ubuntu系统升级到了Ubuntu 16.04 (之前是15.04), 每次开机竟然耗时2分钟左右, 简直不能忍! 咋能向Windows看齐(Windows都没这么慢好嘛), 话说以前的Macbook Pro高配开机只要2秒钟...

那么如何找出拖慢开机速度的元凶并将其禁用呢?

systemd自带分析利器systemd-analyze

systemd-analyze有一些参数，大体上来讲就是一些功能, 可以用

man systemd-analyze

来看看:

!(http://p3.pstatp.com/large/b0a00043c640655af2b)

其中, 对我们比较有用的参数就是blame。

blame : blame是"指责"的意思。

如果你平时使用git来进行版本控制, 那你应该知道有个命令叫

git blame

用git blame加文件名就可以看到文件中每一行最近是谁更改的。

同理, systemd-analyze blame就是显示开机启动项的时间, 从最慢依次列出。

systemd-analyze blame

!(http://p3.pstatp.com/large/b0d0004f63841d29b28)

可以看到, 最慢的启动项是NetworkManager-wait-online.service, 竟然用了30秒, 不能忍!

禁用之:

sudo systemctl disable NetworkManager-wait-online.service

!(http://p3.pstatp.com/large/ac70007fa3b7e039b2d)

其实, 昨天我还禁用了两个启动项, 都是fstab中的, 用于挂载(mount)服务器上的nfs磁盘, 每一个竟然用了1分钟左右 (用systemd-analyze blame看到的):

sudo vim /etc/fstab

!(http://p3.pstatp.com/large/b0c0004fc659a691d43)

就是上图中

172.19.0.133:/mnt/androidstorage/NFS_RO /mnt/nfs_ro

172.19.0.133:/mnt/androidstorage/NFS_RW /mnt/nfs_rw

那两行, 表示

把远端服务器172.19.0.133上的/mnt/androidstorage/NFS_RO挂载(sudo mount)到本机的/mnt/nfs_ro

把远端服务器172.19.0.133上的/mnt/androidstorage/NFS_RW挂载(sudo mount)到本机的/mnt/nfs_rw

如何禁用呢?

在options那一列中加上 noauto即可, 表示不自动启动。

auto是automatic的缩写, 表示"自动"。而noauto就是not/no automatic的缩写, 表示"不自动"。

!(http://p2.pstatp.com/large/b0e0004f3e8437b221a)

用man fstab可以看到:

!(http://p1.pstatp.com/large/ac8000740b1ca4e548a)

!(http://p3.pstatp.com/large/b0e0004f64645ae91b5)

可以看到, 默认情况下(default中)是auto的, 也就是说默认情况下会在开机时自动挂载那两个nfs磁盘。

如此处理之后, 再次重启电脑:

sudo reboot

这回, 电脑启动只要几秒啦!

所以, 如果想加快开机启动, 只要先用

systemd-analyze blame

列出开机启动项的时间, 然后禁用那些很慢的即可, 不过也要小心, 不要近用了系统的关键启动项。
