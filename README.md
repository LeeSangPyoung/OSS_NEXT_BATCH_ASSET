### 1.	배치스케줄러 프로젝트 구조
- 1\)	실행환경
	- JDK 버전 : 권장사항은 jdk 1.6이며, 아래 설명은 jdk 23으로 진행하였음.
	- 데이터 베이스 지원하는 데이터베이스는 다음과 같습니다. 초기 DB 구축에 대한 DDL을 참조하여, 아래 DB는 지원하는 것으로 간주함.
 ![Aspose Words 85b272cf-0469-40d0-aa94-6fa320f606ae 001](https://github.com/user-attachments/assets/3e3fd70c-4310-45d7-90e5-949dfd62a2d6)

- 2\)	구성요소
	- nexcore.scheduler.agent: 작업 실행 엔진으로, 배치 작업을 스케줄에 따라 실행합니다. 해당 모듈은 server에서 lib로 참조하게 된다.
	- nexcore.scheduler.server: UI 및 API 관리 서버로, 작업 상태를 확인하고 모니터링합니다.
	- nexcore.scheduler.common: 공통 모듈로, 데이터베이스와의 상호작용 및 작업 정의를 지원합니다. 해당 모듈은 server, agent에서 lib로 참조하게 된다.
- 3\)	배치스케줄러 주 프로세스
	- Job 등록
  		- a\) 관리자는 Server의 UI(JSP 기반) 또는 REST API를 통해 작업을 등록합니다.
   			- JSP 파일: src/web/jobmon/view_jobdef.jsp (작업 정의 화면)
   			- Controller 클래스: ControllerServiceImpl.java
  		- b\)	작업 정의는 데이터베이스에 저장되며, SQL 매핑 파일에서 정의된 작업 정보(.xsql)로 관리됩니다.
	- 배치스케줄러 작업 스케줄링 (Quartz 사용)
  		- a\)	작업이 등록되면, JobDefinitionManager 클래스가 데이터베이스에서 작업 스케줄 정보를 가져옵니다.
  		- b\)	작업 실행 시점(nextrundate)과 반복 주기(repeat_interval)을 기반으로 작업 대기열에 추가됩니다. 
	- 작업실행
  		- a\)	작업실행은 실제 agent에서 수행하며 nextrundate를 참조하여 job이 실행되는 시점이 되었을 경우 수행한다.


    
### 2.	Batch 스케줄러 사용법 (실제 JOB 등록 및 테스트)
- 1\)	로그인 : 초기 비밀번호는 admin / nexcore 이다. (user 테이블에 초기 INSERT하는 쿼리를 수행해줘야한다. – 소스 설명에서 참조)
  	![image](https://github.com/user-attachments/assets/ec05e526-ded8-468d-aa57-3742d47192a7)

- 2\)	agent가 이미 서버에 설치가 되어있다는 가정하에 서버를 등록(메뉴 : 서버) 하여 주고, 서버와 통신이 원할하게 되고 있는지 확인한다. (녹색 : 통신중)
  
  	![image](https://github.com/user-attachments/assets/4b7a6fd9-c3fc-49d7-8a59-71f16484cc5d)

- 3\)	Job은 그룹단위로 관리가 되고 있으며, 상위개념인 group을 먼저 만들어 준다. (메뉴 : Job 그룹)
  
  	![image](https://github.com/user-attachments/assets/60d6bc1a-d404-4188-abe8-0b210b1b1dbe)


- 4\)	Job 등록 정보에서 Job을 등록하여 준다.
  
  	![image](https://github.com/user-attachments/assets/7bfd55b5-ead1-4c07-828f-0e37a1657f90)

	- 해당 배치는 실행을 sleep 10을 실행시키는 배치이며, 00:00-23:59가 유효시간이고 반복주기가 10초 이므로, 매일 00:00부터 10초마다 sleep 명령어를 실행시키게 된다.
   
   	![image](https://github.com/user-attachments/assets/0f34377d-6e97-4f4f-a6a8-381eec27ea41)

	- 등록시에 ‘자동승인’을 체크하지 않으면, 별도 관리자가 승인 메뉴(Job등록요청현황 메뉴)에서 승인 해줘야 등록이 완료된다. ‘자동승인’을 체크하면 승인 필요없이 바로 job등록이 완료된다.
   
   	![image](https://github.com/user-attachments/assets/4932c44a-b449-4a2e-bfa9-6834cb9c72ad)

	- 실제 agent에서 구동할 수 있게 하려면 job을 통해서 ‘인스턴스’를 생성하여야 한다. 그러므로 해당 job을 체크하고 ‘인스턴스 생성’을 하여 준다. (Lock : Lock이 선택되면 인스턴스가 생성은 되지만 실행은 안되고, Lock을 해제하게 되면 인스턴스 생성과 동시에 바로 실행이 되어버린다.)
- 5\)	Job 인스턴스 메뉴에서 등록한 job이 잘 동작하는지 모니터링할 수 있다.
  
  	![image](https://github.com/user-attachments/assets/04d08a56-3e90-4d36-973b-e3226ccb2a1f)

	- (인스턴스 전체 현황 모니터링)
   
 	![image](https://github.com/user-attachments/assets/8aca99fc-49e7-4e4d-8e2a-9e2f38dd87cf)

	- (인스턴스 세부항목 별로 모니터링을 할 수 있다)

### 3.	Nexcore Batch Scheduler Source 소개 
- 1\)	Batch-scheduler : 스케줄러의 백엔드와 UI 부분를 모두 관리하며, 해당 폴더는 아래와 같습니다. 
	- UI는 자체 Jetty Server를 사용하며 6버전대를 사용하며 해당 버전은 java 1.6 버전에java를 상위버종속되어 있어서 상위 사용하기 위해서는 1.6버전에 종속되어 있어서 업데이트 부분이 필요하며, 현재 1.8 버전에서 compile은 가능한 수준이며, WEB은 compile이 되지 않아 향후 조정 필요.
	- 비즈니스 로직 부분은 아래와 같으며, 패키지간에 상위 패키지를 lib로 사용 중이며, 주 소스 server, agent는 아래와 같이 lib를 참조하여 사용한다.
	- nexcore-scheduler-server : nexcore-scheduler-common, nexcore-scheduler-agent 참조
	- nexcore-scheduler-agent : nexcore-scheduler-common 참조

- 2\)	주요 설정 정보 
	- nexcore-scheduler-server.properties
	- scheduler.db.vendor : 사용할 DB를 선택한다.
	- scheduler.jdbc.* : DB 접속 정보를 입력한다.


	![image](https://github.com/user-attachments/assets/01da4442-76c9-4445-8d99-800fa2ce7bea)


	- (scheduler-agent)
 
	![image](https://github.com/user-attachments/assets/94f1c294-f58b-4135-9ae7-8ecb004dd20a)
