
# mic

- 微服务监控和管理中心

## 功能
- 各节点向mic注册
- 监视各节点的运行状态
- mic管理各节点中服务器的运行和停止
- 查看各节点之间向后连接的情况
- 支持远程向节点部署应用
- 查看节点stub api（如果节点应用支持的话）

## 依赖
- 依赖用户中心:uc
- mongodb

## 用法
- 注册到mic的网关节点配置：
 位置：conf/mic-registry.json
 
	{
		"guid":"8E8710A9-11EB-4F38-B901-657538057BA1",
		"title":"源码网关",
		"enabled":true,
		"desc":"源码网关向mic注册",
		"mic":{
			"location":"/rr/mm/",
			"host":"tcp://localhost:7080?heartbeat=5000&initialWireSize=1&workThreadCount=2&acceptErrorPath=/website/error/",
			"reconnDelay":6000,
			"reconnPeriod":15000,
			"cjtoken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaiIsInBhc3N3b3JkIjoiMTEiLCJleHAiOjExNzQ2OTMwMzIwNjUsInVzZXIiOiJjaiIsImlhdCI6MTU1MzgzMjA2NSwianRpIjoiY2Q2MWQwYmEtNDhjMy00NzNkLTlkYTMtMDBlZGFlYzFkNDc2In0.1Yq4mMYTYO0nzeWSpg2wJhgUtK-e2tdKf1f3fM3mh0Q"
		}
	}


![主页面](https://github.com/carocean/cj.studio.mic/blob/master/documents/img/mic.png)

![mic图2](https://github.com/carocean/cj.studio.mic/blob/master/documents/img/mic2.png)