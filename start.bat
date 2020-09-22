@echo off
rem 该脚本为window下的启动脚本
rem Author:JackIce

set JAVA_HOME=E:\jdk1.8.0
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar
set JAR=starter-1.0-SNAPSHOT.jar
set APP_MAINCLASS=starter.StarterApplication
set APP_HOME=D:\jackicetest
set JAVA_OPTS=-server -Xms512m -Xmx2048m -Xmn512m -Djava.awt.headless=true -XX:MaxPermSize=128m
set JAVA_CMD=%JAVA_HOME%\bin\java
set JAVA_EXT_DIRS=%APP_HOME%\lib;%JAVA_HOME%\jre\lib\ext\
rem 启动程序
goto start

rem 启动函数
:start
echo "starting ......."
cd /d %APP_HOME%
%JAVA_CMD% %JAVA_OPTS% -Djava.ext.dirs=%JAVA_EXT_DIRS% -classpath %APP_HOME%\%JAR% %APP_MAINCLASS%
rem JVM:JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xss512k"
rem -server:一定要作为第一个参数，在多个CPU时性能佳
rem -Xms：初始Heap大小，使用的最小内存,cpu性能高时此值应设的大一些
rem -Xmx：Java heap最大值，使用的最大内存
rem 上面两个值是分配JVM的最小和最大内存，取决于硬件物理内存的大小，建议均设为物理内存的一半。
rem -XX:PermSize:设定内存的永久保存区域
rem -XX:MaxPermSize:设定最大内存的永久保存区域
rem -XX:MaxNewSize:
rem -Xss 15120 这使得JBoss每增加一个线程（thread)就会立即消耗15M内存，而最佳值应该是128K,默认值好像是512k.
rem +XX:AggressiveHeap 会使得 Xms没有意义。这个参数让jvm忽略Xmx参数,疯狂地吃完一个G物理内存,再吃尽一个G的swap。
rem -Xss：每个线程的Stack大小
rem -verbose:gc 现实垃圾收集信息
rem -Xloggc:gc.log 指定垃圾收集日志文件
rem -Xmn：young generation的heap大小，一般设置为Xmx的3、4分之一
rem -XX:+UseParNewGC ：缩短minor收集的时间
rem -XX:+UseConcMarkSweepGC ：缩短major收集的时间
rem 提示：此选项在Heap Size 比较大而且Major收集时间较长的情况下使用更合适。
rem PermGen space的全称是Permanent Generation space,是指内存的永久保存区域OutOfMemoryError: PermGen rem rem rem rem  space从表面上看就是内存益出，解决方法也一定是加大内存。说说为什么会内存益出：这一部分用于存放Class和Meta的信息,Class在被 Load的时候被放入PermGen space区域，它和和存放Instance的Heap区域不同,GC(Garbage Collection)不会在主程序运行期对PermGen space进行清理，所以如果你的APP会LOAD很多CLASS的话,就很可能出现PermGen space错误。这种错误常见在web服务器对JSP进行pre compile的时候。改正方法：-Xms256m -Xmx256m -XX:MaxNewSize=256m -XX:MaxPermSize=256m 2、在tomcat中redeploy时出现outofmemory的错误. 可以有以下几个方面的原因: 
rem 1,使用了proxool,因为proxool内部包含了一个老版本的cglib. 
rem 2, log4j,最好不用,只用common-logging 
rem 3, 老版本的cglib,快点更新到最新版。
rem 4，更新到最新的hibernate3.2 3、
pause