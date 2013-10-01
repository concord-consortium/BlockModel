@echo off
cd Client
call buildEchoClient
cd ..
cd Server
call buildEchoServer
cd ..
