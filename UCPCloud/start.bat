@echo off
rem �ýű�Ϊwindow�µ������ű�
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
rem ��������
goto start

rem ��������
:start
echo "starting ......."
cd /d %APP_HOME%
%JAVA_CMD% %JAVA_OPTS% -Djava.ext.dirs=%JAVA_EXT_DIRS% -classpath %APP_HOME%\%JAR% %APP_MAINCLASS%
rem JVM:JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xss512k"
rem -server:һ��Ҫ��Ϊ��һ���������ڶ��CPUʱ���ܼ�
rem -Xms����ʼHeap��С��ʹ�õ���С�ڴ�,cpu���ܸ�ʱ��ֵӦ��Ĵ�һЩ
rem -Xmx��Java heap���ֵ��ʹ�õ�����ڴ�
rem ��������ֵ�Ƿ���JVM����С������ڴ棬ȡ����Ӳ�������ڴ�Ĵ�С���������Ϊ�����ڴ��һ�롣
rem -XX:PermSize:�趨�ڴ�����ñ�������
rem -XX:MaxPermSize:�趨����ڴ�����ñ�������
rem -XX:MaxNewSize:
rem -Xss 15120 ��ʹ��JBossÿ����һ���̣߳�thread)�ͻ���������15M�ڴ棬�����ֵӦ����128K,Ĭ��ֵ������512k.
rem +XX:AggressiveHeap ��ʹ�� Xmsû�����塣���������jvm����Xmx����,���س���һ��G�����ڴ�,�ٳԾ�һ��G��swap��
rem -Xss��ÿ���̵߳�Stack��С
rem -verbose:gc ��ʵ�����ռ���Ϣ
rem -Xloggc:gc.log ָ�������ռ���־�ļ�
rem -Xmn��young generation��heap��С��һ������ΪXmx��3��4��֮һ
rem -XX:+UseParNewGC ������minor�ռ���ʱ��
rem -XX:+UseConcMarkSweepGC ������major�ռ���ʱ��
rem ��ʾ����ѡ����Heap Size �Ƚϴ����Major�ռ�ʱ��ϳ��������ʹ�ø����ʡ�
rem PermGen space��ȫ����Permanent Generation space,��ָ�ڴ�����ñ�������OutOfMemoryError: PermGen rem rem rem rem  space�ӱ����Ͽ������ڴ�������������Ҳһ���ǼӴ��ڴ档˵˵Ϊʲô���ڴ��������һ�������ڴ��Class��Meta����Ϣ,Class�ڱ� Load��ʱ�򱻷���PermGen space�������ͺʹ��Instance��Heap����ͬ,GC(Garbage Collection)�����������������ڶ�PermGen space������������������APP��LOAD�ܶ�CLASS�Ļ�,�ͺܿ��ܳ���PermGen space�������ִ��󳣼���web��������JSP����pre compile��ʱ�򡣸���������-Xms256m -Xmx256m -XX:MaxNewSize=256m -XX:MaxPermSize=256m 2����tomcat��redeployʱ����outofmemory�Ĵ���. ���������¼��������ԭ��: 
rem 1,ʹ����proxool,��Ϊproxool�ڲ�������һ���ϰ汾��cglib. 
rem 2, log4j,��ò���,ֻ��common-logging 
rem 3, �ϰ汾��cglib,�����µ����°档
rem 4�����µ����µ�hibernate3.2 3��
pause