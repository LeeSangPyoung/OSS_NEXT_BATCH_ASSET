/* Job Definition 정보*/
CREATE TABLE NBS_JOB_DEF (
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    JOB_GROUP_ID                   VARCHAR(100)    NOT NULL ,    /*   */
    OWNER                          VARCHAR(100)    NOT NULL ,    /* 담당자 사번이나 이름 */
    JOB_DESC                       VARCHAR(300)             ,    /* Job 설명  */
    TIME_FROM                      VARCHAR(4)               ,    /*   */
    TIME_UNTIL                     VARCHAR(4)               ,    /*   */
    REPEAT_YN                      VARCHAR(1)      NOT NULL ,    /* 반복작업 여부 ['N', 'Y':TRUE] */
    REPEAT_INTVAL                  INTEGER                  ,    /* 반복작업 INTERVAL  */
    REPEAT_INTVAL_GB               VARCHAR(10)              ,    /* 반복작업 INTERVAL 구분 ['START':시작시각기준, 'END':종료시각기준] */
    REPEAT_IF_ERROR                VARCHAR(10)              ,    /* 반복작업시 에러발생 ['STOP':정지, 'IGNORE':무시] */
    REPEAT_MAX_OK                  INTEGER         NOT NULL ,    /* 반복작업시 최대 정상 실행 건수  */
    REPEAT_EXACT_EXP               VARCHAR(100)             ,    /* 반복작업시 EXACT 방식일때의 HHMMSS 의 표현식  */
    CONFIRM_NEED_YN                VARCHAR(5)      NOT NULL ,    /* 승인 필요여부 ['N', 'Y':TRUE] */
    PARALLEL_GROUP                 VARCHAR(100)             ,    /* 동시 실행 제한용. 그룹명  */
    JOB_TYPE                       VARCHAR(20)     NOT NULL ,    /*  ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ] */
    AGENT_NODE                     VARCHAR(50)     NOT NULL ,    /* agent 의 system.id  */
    COMPONENT_NAME                 VARCHAR(500)             ,    /*  Ejb: classname, Pojo:classname, proc:command */
    SCHEDULE_TYPE                  VARCHAR(50)     NOT NULL ,    /*  ['FIXED', 'EXPRESSION'] */
    DAYS_IN_MONTH                  VARCHAR(256)             ,    /* 일 스케줄 ['ALL' | '1/2/3/LD/'...  ] */
    MONTHS                         VARCHAR(100)             ,    /* 월 스케줄 ['ALL' | '1/4/7/10/ ...  ] */
    DAYS_OF_WEEK                   VARCHAR(100)             ,    /* 요일 스케줄 ['W2D7/W2D6/...' | '2D5/..'] */
    WEEKDAY_MONTHDAY_TYPE          VARCHAR(10)              ,    /* 일자와 요일 연산 ['OR', 'AND' ] */
    CALENDAR_ID                    VARCHAR(10)              ,    /* CALENDAR ID ['0', '1', '2, ... ] */
    CALENDAR_EXP                   VARCHAR(100)             ,    /* Calendar 상세식 ["B1/B2", "E1/E2", "B1/E1", ... ] */
    DAY_SCHEDULE_TYPE              VARCHAR(10)              ,    /* 숫자지정방식, 달력 지정방식 ['NUMBER', 'CALENDAR'] */
    BEFORE_AFTER_EXP               VARCHAR(10)              ,    /* 익전일 처리 처리 지정 ['A1':익1일, 'B1':전1일] */
    SHIFT_EXP                      VARCHAR(50)              ,    /* 대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */
    SHIFT_EXP2                     VARCHAR(50)              ,    /* 대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */
    FIXED_DAYS                     VARCHAR(200)             ,    /* SCHEDULE_TYPE='FIXED' 일 경우 ['20100101/20100509/...'] */
    EXTRA_SCHEDULE                 VARCHAR(200)             ,    /* 추가 스케줄 정보  */
    BASE_DATE_CAL_ID               VARCHAR(10)              ,    /* 기준일 처리를 위한 CALENDAR ID  */
    BASE_DATE_LOGIC                VARCHAR(10)              ,    /* 기준일 처리를 위한 SHIFT 계산 일수  */
    LOG_LEVEL                      VARCHAR(10)              ,    /* 기본 로그 레벨 NULL 일 경우 AGENT의 기본 레벨이 적용됨 */
    CREATE_TIME                    VARCHAR(14)     NOT NULL ,    /* 최종 변경 시각  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 시각  */
    CONSTRAINT NBS_JOB_DEF_PK PRIMARY KEY (JOB_ID)
);


/* Job Definition 의 선행 Job 설정*/
CREATE TABLE NBS_JOB_DEF_PREJOB (
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*  동일 Job Id 일지라도 Instance 별로 prejob 설정을 달리 할 수 있음 */
    SEQ                            INTEGER         NOT NULL ,    /* 선행 Job 조건의 순서를 위한 일련번호  */
    PRE_JOB_ID                     VARCHAR(100)    NOT NULL ,    /* 선행 Job Id  */
    OK_FAIL                        VARCHAR(20)     NOT NULL ,    /* 선행 Job 조건이 OK 일때인지, FAIL 일때인지 ['OK', 'FAIL', ...] */
    AND_OR                         VARCHAR(5)               ,    /* 여러 선행 Job 간의 And / Or 논리식 ['AND',  'OR'] */
    CONSTRAINT NBS_JOB_DEF_PREJOB_PK PRIMARY KEY (JOB_ID,SEQ)
);


/* Job Definition 의 Trigger 설정*/
CREATE TABLE NBS_JOB_DEF_TRIGGER (
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    SEQ                            INTEGER         NOT NULL ,    /* Trigger Job 조건의 순서를 위한 일련번호  */
    ACTIVATE_WHEN                  VARCHAR(20)     NOT NULL ,    /* Trigger 이벤트 END, ENDOK, ENDFAIL, RETVAL */
    CHECK_VALUE1                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE2                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE3                   VARCHAR(100)             ,    /*   */
    TRIGGER_JOB_ID                 VARCHAR(100)    NOT NULL ,    /* Trigger 될 JOB_ID  */
    INSTANCE_COUNT                 INTEGER                  ,    /* Trigger 될 Job 인스턴스 개수  */
    CONSTRAINT NBS_JOB_DEF_TRIGGER_PK PRIMARY KEY (JOB_ID,SEQ)
);


/* Job Definition 의 파라미터 설정*/
CREATE TABLE NBS_JOB_DEF_PARAM (
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    SEQ                            INTEGER         NOT NULL ,    /* 파라미터 순서를 위한 일련번호  */
    PARAM_NAME                     VARCHAR(100)    NOT NULL ,    /* 파라미터 이름  */
    PARAM_VALUE                    VARCHAR(100)             ,    /* 파라미터 값 $를 이용하여 식을 넣을수도 있음 */
    CONSTRAINT NBS_JOB_DEF_PARAM_PK PRIMARY KEY (JOB_ID,PARAM_NAME)
);


/* */
CREATE TABLE NBS_PARALLEL_GROUP (
    GROUP_NAME                     VARCHAR(50)     NOT NULL ,    /* CONCURRENT 그룹명  */
    GROUP_DESC                     VARCHAR(300)    NOT NULL ,    /* CONCURRENT 그룹 설명  */
    MAX_LIMIT                      INTEGER         NOT NULL ,    /* 최대 동시 실행 수  */
    CONSTRAINT NBS_PARALLEL_GROUP_PK PRIMARY KEY (GROUP_NAME)
);


/* 전역 파라미터 설정*/
CREATE TABLE NBS_GLOBAL_PARAM (
    PARAM_NAME                     VARCHAR(100)    NOT NULL ,    /* 파라미터 이름  */
    PARAM_VALUE                    VARCHAR(100)             ,    /* 파라미터 값 $를 이용하여 식을 넣을수도 있음 */
    CONSTRAINT NBS_GLOBAL_PARAM_PK PRIMARY KEY (PARAM_NAME)
);


/* Job Definition 변경/신규 요청정보*/
CREATE TABLE NBS_JOB_DEF_STG (
    REQ_NO                         VARCHAR(50)     NOT NULL ,    /* 요청 번호  */
    REQ_USERNAME                   VARCHAR(50)     NOT NULL ,    /* 요청자 ID  */
    REQ_USER_IP                    VARCHAR(50)     NOT NULL ,    /* 요청자 IP  */
    REQ_TIME                       VARCHAR(14)     NOT NULL ,    /* 요청시각  */
    REQ_TYPE                       VARCHAR(10)     NOT NULL ,    /*  ADD': 신규, 'EDIT':변경, 'DELETE':삭제 */
    REQ_COMMENT                    VARCHAR(200)             ,    /* 요청 기타 내용  */
    REQ_STATE                      VARCHAR(20)     NOT NULL ,    /* 요청 상태 Q':요청, 'A(YYYYMMDDHHMMSS):승인', 'R(YYYYMMDDHHMMSS):반려 */
    REQ_AR_REASON                  VARCHAR(400)             ,    /* 승인, 반려 사유  */
    REQ_OPER_ID                    VARCHAR(50)              ,    /* 승인자 ID  */
    REQ_OPER_NAME                  VARCHAR(50)              ,    /* 승인자 이름  */
    REQ_OPER_IP                    VARCHAR(50)              ,    /* 승인자 IP  */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    JOB_GROUP_ID                   VARCHAR(100)    NOT NULL ,    /*   */
    OWNER                          VARCHAR(100)    NOT NULL ,    /* 담당자 사번이나 이름 */
    JOB_DESC                       VARCHAR(300)             ,    /* Job 설명  */
    TIME_FROM                      VARCHAR(4)               ,    /*   */
    TIME_UNTIL                     VARCHAR(4)               ,    /*   */
    REPEAT_YN                      VARCHAR(1)      NOT NULL ,    /* 반복작업 여부 ['N', 'Y':TRUE] */
    REPEAT_INTVAL                  INTEGER                  ,    /* 반복작업 INTERVAL  */
    REPEAT_INTVAL_GB               VARCHAR(10)              ,    /* 반복작업 INTERVAL 구분 ['START':시작시각기준, 'END':종료시각기준] */
    REPEAT_IF_ERROR                VARCHAR(10)              ,    /* 반복작업시 에러발생 ['STOP':정지, 'IGNORE':무시] */
    REPEAT_MAX_OK                  INTEGER         NOT NULL ,    /* 반복작업시 최대 정상 실행 건수  */
    REPEAT_EXACT_EXP               VARCHAR(100)             ,    /* 반복작업시 EXACT 방식일때의 HHMMSS 의 표현식  */
    CONFIRM_NEED_YN                VARCHAR(5)      NOT NULL ,    /* 승인 필요여부 ['N', 'Y':TRUE] */
    PARALLEL_GROUP                 VARCHAR(100)             ,    /* 동시 실행 제한용. 그룹명  */
    JOB_TYPE                       VARCHAR(20)     NOT NULL ,    /*  ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ] */
    AGENT_NODE                     VARCHAR(50)     NOT NULL ,    /* agent 의 system.id  */
    COMPONENT_NAME                 VARCHAR(500)             ,    /*  Ejb: classname, Pojo:classname, proc:command */
    SCHEDULE_TYPE                  VARCHAR(50)     NOT NULL ,    /*  ['FIXED', 'EXPRESSION'] */
    DAYS_IN_MONTH                  VARCHAR(256)             ,    /* 일 스케줄 ['ALL' | '1/2/3/LD/'...  ] */
    MONTHS                         VARCHAR(100)             ,    /* 월 스케줄 ['ALL' | '1/4/7/10/ ...  ] */
    DAYS_OF_WEEK                   VARCHAR(100)             ,    /* 요일 스케줄 ['W2D7/W2D6/...' | '2D5/..'] */
    WEEKDAY_MONTHDAY_TYPE          VARCHAR(10)              ,    /* 일자와 요일 연산 ['OR', 'AND' ] */
    CALENDAR_ID                    VARCHAR(10)              ,    /* CALENDAR ID ['0', '1', '2, ... ] */
    CALENDAR_EXP                   VARCHAR(100)             ,    /* Calendar 상세식 ["B1/B2", "E1/E2", "B1/E1", ... ] */
    DAY_SCHEDULE_TYPE              VARCHAR(10)              ,    /* 숫자지정방식, 달력 지정방식 ['NUMBER', 'CALENDAR'] */
    BEFORE_AFTER_EXP               VARCHAR(10)              ,    /* 익전일 처리 처리 지정 ['A1':익1일, 'B1':전1일] */
    SHIFT_EXP                      VARCHAR(50)              ,    /* 대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */
    SHIFT_EXP2                     VARCHAR(50)              ,    /* 대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */
    FIXED_DAYS                     VARCHAR(200)             ,    /* SCHEDULE_TYPE='FIXED' 일 경우 ['20100101/20100509/...'] */
    EXTRA_SCHEDULE                 VARCHAR(200)             ,    /* 추가 스케줄 정보  */
    BASE_DATE_CAL_ID               VARCHAR(10)              ,    /* 기준일 처리를 위한 CALENDAR ID  */
    BASE_DATE_LOGIC                VARCHAR(10)              ,    /* 기준일 처리를 위한 SHIFT 계산 일수  */
    LOG_LEVEL                      VARCHAR(10)              ,    /* 기본 로그 레벨 NULL 일 경우 AGENT의 기본 레벨이 적용됨 */
    CREATE_TIME                    VARCHAR(14)     NOT NULL ,    /* 최종 변경 시각  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 시각  */
    CONSTRAINT NBS_JOB_DEF_STG_PK PRIMARY KEY (REQ_NO)
);
CREATE INDEX NBS_JOB_DEF_STG_IDX2 ON NBS_JOB_DEF_STG(REQ_TIME);
CREATE INDEX NBS_JOB_DEF_STG_IDX3 ON NBS_JOB_DEF_STG(REQ_STATE);
CREATE UNIQUE INDEX NBS_JOB_DEF_STG_IDX1 ON NBS_JOB_DEF_STG(JOB_ID,REQ_STATE);


/* Job Definition 의 선행 Job 설정*/
CREATE TABLE NBS_JOB_DEF_PREJOB_STG (
    REQ_NO                         VARCHAR(50)     NOT NULL ,    /* 요청 번호  */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*  동일 Job Id 일지라도 Instance 별로 prejob 설정을 달리 할 수 있음 */
    SEQ                            INTEGER         NOT NULL ,    /* 선행 Job 조건의 순서를 위한 일련번호  */
    PRE_JOB_ID                     VARCHAR(100)    NOT NULL ,    /* 선행 Job Id  */
    OK_FAIL                        VARCHAR(20)     NOT NULL ,    /* 선행 Job 조건이 OK 일때인지, FAIL 일때인지 ['OK', 'FAIL', ...] */
    AND_OR                         VARCHAR(5)               ,    /* 여러 선행 Job 간의 And / Or 논리식 ['AND',  'OR'] */
    CONSTRAINT NBS_JOB_DEF_PREJOB_STG_PK PRIMARY KEY (REQ_NO,JOB_ID,SEQ)
);


/* Job Definition 의 Trigger 설정*/
CREATE TABLE NBS_JOB_DEF_TRIGGER_STG (
    REQ_NO                         VARCHAR(50)     NOT NULL ,    /* 요청 번호  */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    SEQ                            INTEGER         NOT NULL ,    /* Trigger Job 조건의 순서를 위한 일련번호  */
    ACTIVATE_WHEN                  VARCHAR(20)     NOT NULL ,    /* Trigger 이벤트 END, ENDOK, ENDFAIL, RETVAL */
    CHECK_VALUE1                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE2                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE3                   VARCHAR(100)             ,    /*   */
    TRIGGER_JOB_ID                 VARCHAR(100)    NOT NULL ,    /* Trigger 될 JOB_ID  */
    INSTANCE_COUNT                 INTEGER                  ,    /* Trigger 될 Job 인스턴스 개수  */
    CONSTRAINT NBS_JOB_DEF_TRIGGER_STG_PK PRIMARY KEY (REQ_NO,JOB_ID,SEQ)
);


/* Job Definition 의 파라미터 설정*/
CREATE TABLE NBS_JOB_DEF_PARAM_STG (
    REQ_NO                         VARCHAR(50)     NOT NULL ,    /* 요청 번호  */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    SEQ                            INTEGER         NOT NULL ,    /* 파라미터 순서를 위한 일련번호  */
    PARAM_NAME                     VARCHAR(100)    NOT NULL ,    /* 파라미터 이름  */
    PARAM_VALUE                    VARCHAR(100)             ,    /* 파라미터 값 $를 이용하여 식을 넣을수도 있음 */
    CONSTRAINT NBS_JOB_DEF_PARAM_STG_PK PRIMARY KEY (REQ_NO,JOB_ID,PARAM_NAME)
);


/* */
CREATE TABLE NBS_JOB_INS (
    PROC_DATE                      VARCHAR(8)      NOT NULL ,    /*   */
    BASE_DATE                      VARCHAR(8)      NOT NULL ,    /* 기준일 달력 기반으로 설정한 날짜 */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    JOB_GROUP_ID                   VARCHAR(100)    NOT NULL ,    /*   */
    JOB_INSTANCE_ID                VARCHAR(100)    NOT NULL ,    /*   */
    JOB_DESC                       VARCHAR(300)             ,    /* Job 설명  */
    JOB_STATE                      VARCHAR(2)      NOT NULL ,    /*  ['I':init, 'W':wait, 'EO':ok, 'EF':fail, 'R':run, 'S':suspended] */
    JOB_STATE_REASON               VARCHAR(400)             ,    /* 현재 상태 사유 어떤 조건에 의해 WAIT 하고 있는지를 기술 */
    LAST_JOB_EXE_ID                VARCHAR(100)             ,    /* 최종 JobExecution Id 상태 'R' 로 update 될때 동시에 update됨 */
    LOCKED_BY                      VARCHAR(50)              ,    /* null or operator ID LOCK 상태라면 operator ID가 여기 들어감 */
    TIME_FROM                      VARCHAR(4)               ,    /*   */
    TIME_UNTIL                     VARCHAR(4)               ,    /*   */
    REPEAT_YN                      VARCHAR(1)      NOT NULL ,    /* 반복작업 여부 ['N', 'Y':TRUE] */
    REPEAT_INTVAL                  INTEGER                  ,    /* 반복작업 INTERVAL  */
    REPEAT_INTVAL_GB               VARCHAR(10)              ,    /* 반복작업 INTERVAL 구분 ['START':시작시각기준, 'END':종료시각기준] */
    REPEAT_IF_ERROR                VARCHAR(10)              ,    /* 반복작업시 에러발생 ['STOP':정지, 'IGNORE':무시] */
    REPEAT_MAX_OK                  INTEGER         NOT NULL ,    /* 반복작업시 최대 정상 실행 건수  */
    REPEAT_EXACT_EXP               VARCHAR(100)             ,    /* 반복작업시 EXACT 방식일때의 HHMMSS 의 표현식  */
    CONFIRM_NEED_YN                VARCHAR(5)      NOT NULL ,    /* 승인 필요여부 ['N', 'Y':TRUE] */
    CONFIRMED                      VARCHAR(50)              ,    /* 승인 여부 [시각+승인자ID+승인자IP] */
    PARALLEL_GROUP                 VARCHAR(100)             ,    /* 동시 실행 제한용. 그룹명  */
    JOB_TYPE                       VARCHAR(20)     NOT NULL ,    /* 0 ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ] */
    AGENT_NODE                     VARCHAR(50)     NOT NULL ,    /* agent 의 NEXCORE_ID  */
    LAST_AGENT_NODE                VARCHAR(50)              ,    /* 최종 실행된 Agent 의 NEXCORE_ID  */
    COMPONENT_NAME                 VARCHAR(500)             ,    /* 0 JBATCH: classname, POJO:classname, PROC:command */
    ACTIVATION_TIME                VARCHAR(14)              ,    /* instance 화 된 시각  */
    ACTIVATOR                      VARCHAR(100)             ,    /* instance 화한 주체  */
    RUN_COUNT                      INTEGER         NOT NULL ,    /* 실행횟수  */
    END_OK_COUNT                   INTEGER         NOT NULL ,    /* 정상 종료 횟수  */
    LOG_LEVEL                      VARCHAR(10)              ,    /* 기본 로그 레벨 NULL 일 경우 AGENT의 기본 레벨이 적용됨 */
    LAST_START_TIME                VARCHAR(14)              ,    /* 최종 start 시각  */
    LAST_END_TIME                  VARCHAR(14)              ,    /* 최종 종료 시각 시각  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 시각  */
    CONSTRAINT NBS_JOB_INS_PK PRIMARY KEY (JOB_INSTANCE_ID)
);
CREATE INDEX NBS_JOB_INS_IDX2 ON NBS_JOB_INS(JOB_STATE,PARALLEL_GROUP);
CREATE INDEX NBS_JOB_INS_IDX1 ON NBS_JOB_INS(PROC_DATE,LAST_MODIFY_TIME);
CREATE INDEX NBS_JOB_INS_IDX4 ON NBS_JOB_INS(ACTIVATION_TIME,LAST_MODIFY_TIME);


/* Instance 의 선행 Job 설정*/
CREATE TABLE NBS_JOB_INS_PREJOB (
    JOB_INSTANCE_ID                VARCHAR(100)    NOT NULL ,    /*  동일 Job Id 일지라도 Instance 별로 prejob 설정을 달리 할 수 있음 */
    SEQ                            INTEGER         NOT NULL ,    /* 선행 Job 조건의 순서를 위한 일련번호  */
    PRE_JOB_ID                     VARCHAR(100)    NOT NULL ,    /* 선행 Job Id  */
    OK_FAIL                        VARCHAR(20)     NOT NULL ,    /* 선행 Job 조건이 OK 일때인지, FAIL 일때인지 ['OK', 'FAIL', ...] */
    AND_OR                         VARCHAR(5)               ,    /* 여러 선행 Job 간의 And / Or 논리식 ['AND',  'OR'] */
    CONSTRAINT NBS_JOB_INS_PREJOB_PK PRIMARY KEY (JOB_INSTANCE_ID,SEQ)
);


/* Job Instance 의 Trigger 설정*/
CREATE TABLE NBS_JOB_INS_TRIGGER (
    JOB_INSTANCE_ID                VARCHAR(100)    NOT NULL ,    /*   */
    SEQ                            INTEGER         NOT NULL ,    /* Trigger Job 조건의 순서를 위한 일련번호  */
    ACTIVATE_WHEN                  VARCHAR(20)     NOT NULL ,    /* Trigger 이벤트 END, ENDOK, ENDFAIL, RETVAL */
    CHECK_VALUE1                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE2                   VARCHAR(100)             ,    /*   */
    CHECK_VALUE3                   VARCHAR(100)             ,    /*   */
    TRIGGER_JOB_ID                 VARCHAR(100)    NOT NULL ,    /* Trigger 될 JOB_ID  */
    INSTANCE_COUNT                 INTEGER                  ,    /* Trigger 될 Job 인스턴스 개수  */
    CONSTRAINT NBS_JOB_INS_TRIGGER_PK PRIMARY KEY (JOB_INSTANCE_ID,SEQ)
);


/* 인스턴스 파라미터를 담아두는 공간*/
CREATE TABLE NBS_JOB_INS_OBJ_STORE (
    JOB_INSTANCE_ID                VARCHAR(100)    NOT NULL ,    /*   */
    DATA_TYPE                      VARCHAR(2)      NOT NULL ,    /*  P':파라미터 */
    DATA_XML                       CLOB                     ,    /*   */
    CONSTRAINT NBS_JOB_INS_OBJ_STORE_PK PRIMARY KEY (JOB_INSTANCE_ID,DATA_TYPE)
);


/* Job Execution 정보가 저정되는 테이블*/
CREATE TABLE NBS_JOB_EXE (
    PROC_DATE                      VARCHAR(8)      NOT NULL ,    /*   */
    BASE_DATE                      VARCHAR(8)      NOT NULL ,    /* 기준일 달력 기반으로 설정한 날짜 */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    JOB_INSTANCE_ID                VARCHAR(100)    NOT NULL ,    /*   */
    JOB_EXECUTION_ID               VARCHAR(100)    NOT NULL ,    /*   */
    JOB_TYPE                       VARCHAR(20)     NOT NULL ,    /*  ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ] */
    AGENT_NODE                     VARCHAR(50)     NOT NULL ,    /* agent 의 system.id  */
    EXE_STATE                      SMALLINT        NOT NULL ,    /*  [1=INIT, 2=RUNNING, 4=SUSPENDED, 7=ENDED, 99=Unknown ] */
    RUN_COUNT                      INTEGER         NOT NULL ,    /* 실행횟수  */
    START_TIME                     VARCHAR(20)              ,    /* start 시각  */
    END_TIME                       VARCHAR(20)              ,    /* 종료 시각 시각  */
    RETURN_CODE                    INTEGER                  ,    /* 종료 리턴코드 0이면 정상종료, 그외는 에러 */
    ERROR_MSG                      VARCHAR(1000)            ,    /* 에러발생시 에러메세지  */
    PROGRESS_CURRENT               BIGINT                   ,    /* 최종 처리 건수  */
    PROGRESS_TOTAL                 BIGINT                   ,    /* 전체 처리건수  */
    OPERATOR_TYPE                  VARCHAR(10)              ,    /* 조작자 유형 SCH,USR,OND */
    OPERATOR_ID                    VARCHAR(20)              ,    /* 조작자 ID  */
    OPERATOR_IP                    VARCHAR(20)              ,    /* 조작자 IP  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 시각  */
    CONSTRAINT NBS_JOB_EXE_PK PRIMARY KEY (JOB_EXECUTION_ID)
);
CREATE INDEX NBS_JOB_EXE_IDX1 ON NBS_JOB_EXE(JOB_INSTANCE_ID,EXE_STATE,JOB_EXECUTION_ID);
CREATE INDEX NBS_JOB_EXE_IDX2 ON NBS_JOB_EXE(JOB_EXECUTION_ID,RETURN_CODE);


/* 파라미터 값과 리턴값을 담아놓는 STORE*/
CREATE TABLE NBS_JOB_EXE_OBJ_STORE (
    JOB_EXECUTION_ID               VARCHAR(100)    NOT NULL ,    /*   */
    DATA_TYPE                      VARCHAR(2)      NOT NULL ,    /*  P':파라미터, 'R':리턴값 */
    DATA_XML                       CLOB                     ,    /*   */
    CONSTRAINT NBS_JOB_EXE_OBJ_STORE_PK PRIMARY KEY (JOB_EXECUTION_ID,DATA_TYPE)
);


/* DAILY ACTIVATION 내역을 담아놓는 테이블*/
CREATE TABLE NBS_ACTIVATION_LOG (
    ACTIVATION_TIME                VARCHAR(20)     NOT NULL ,    /*   */
    PROC_DATE                      VARCHAR(8)      NOT NULL ,    /*   */
    SYSTEM_ID                      VARCHAR(20)     NOT NULL ,    /* 스케줄러 인스턴스 ID  */
    JOB_INS_COUNT                  INTEGER         NOT NULL ,    /* Job Instance Id  */
    JOB_INS_ID_LIST                CLOB                     ,    /* 콤마로 구분된 Job Instance Id 리스트  */
    CONSTRAINT NBS_ACTIVATION_LOG_PK PRIMARY KEY (PROC_DATE)
);


/* 뷰필터 마스터 테이블*/
CREATE SEQUENCE NBS_VIEW_FILTER_SEQ START WITH 1;
CREATE TABLE NBS_VIEW_FILTER (
    VF_ID                          INTEGER         NOT NULL ,    /*   */
    VF_NAME                        VARCHAR(100)    NOT NULL ,    /* 뷰필터명  */
    VF_TEAM                        VARCHAR(100)    NOT NULL ,    /* 담당자팀  */
    VF_OWNER                       VARCHAR(50)     NOT NULL ,    /* 담당자명  */
    VF_DESC                        VARCHAR(200)             ,    /* 설명  */
    JOB_COUNT                      INTEGER         NOT NULL ,    /* JOB 개수  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종수정일  */
    CONSTRAINT NBS_VIEW_FILTER_PK PRIMARY KEY (VF_ID)
);


/* 뷰필터 마스터 테이블*/
CREATE TABLE NBS_VIEW_FILTER_DTL (
    VF_ID                          INTEGER         NOT NULL ,    /*   */
    JOB_ID                         VARCHAR(100)    NOT NULL     /* 대상 JobId  */
);
CREATE INDEX NBS_VIEW_FILTER_DTL_IDX1 ON NBS_VIEW_FILTER_DTL(VF_ID);


/* 에이전트 관리 테이블*/
CREATE TABLE NBS_AGENT (
    AGENT_ID                       VARCHAR(20)     NOT NULL ,    /* AGENT 의 system.id  */
    AGENT_NAME                     VARCHAR(100)    NOT NULL ,    /* AGENT 의 이름  */
    AGENT_DESC                     VARCHAR(200)    NOT NULL ,    /* AGENT 의 설명  */
    AGENT_IP                       VARCHAR(20)     NOT NULL ,    /* AGENT 의 통신 IP  */
    AGENT_PORT                     INTEGER         NOT NULL ,    /* AGENT 의 통신 Port  */
    RUN_MODE                       VARCHAR(2)      NOT NULL ,    /* 구동 모드 S':Standalone, 'W':WAS based */
    IN_USE                         VARCHAR(1)      NOT NULL ,    /* 사용중인 에이전트인지 1', '0' */
    BASE_DIRECTORY                 VARCHAR(200)             ,    /* 설치 기준 디렉토리 $NEXCORE_HOME */
    OS_USERID                      VARCHAR(20)              ,    /* 구동 OS 계정 unix일 경우 */
    OS_PASSWD                      VARCHAR(50)              ,    /* 구동 OS 비번 암호화됨 */
    START_CMD                      VARCHAR(100)             ,    /* 기동 스크립트 예) startup.sh */
    REMOTE_START_TYPE              VARCHAR(10)              ,    /* 원격 START용 접속 URL TELNET', 'SSH', 'LOCAL' */
    MAX_RUNNING_JOB                INTEGER                  ,    /* 최대 동시 실행 Job 수 미지정시 무제한 */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종수정일  */
    CONSTRAINT NBS_AGENT_PK PRIMARY KEY (AGENT_ID)
);


/* Job 통지 설정 테이블*/
CREATE SEQUENCE NBS_NOTIFY_SEQ START WITH 1;
CREATE TABLE NBS_NOTIFY (
    NOTIFY_ID                      INTEGER         NOT NULL ,    /* PK  */
    NOTIFY_DESC                    VARCHAR(200)             ,    /* 설명  */
    JOB_ID_EXP                     VARCHAR(200)    NOT NULL ,    /* JOB ID 정규표현식  */
    NOTIFY_WHEN                    VARCHAR(50)     NOT NULL ,    /*  EO':정상종료시, 'EF':에러종료시, 'LONGRUN':장기수행 */
    CHECK_VALUE1                   VARCHAR(20)              ,    /*   */
    CHECK_VALUE2                   VARCHAR(20)              ,    /*   */
    CHECK_VALUE3                   VARCHAR(20)              ,    /*   */
    RECEIVERS                      VARCHAR(200)             ,    /* 통지 수신자 리스트  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종수정일  */
    CONSTRAINT NBS_NOTIFY_PK PRIMARY KEY (NOTIFY_ID)
);


/* Job 통지 설정 테이블*/
CREATE SEQUENCE NBS_NOTIFY_RECEIVER_SEQ START WITH 1;
CREATE TABLE NBS_NOTIFY_RECEIVER (
    RECEIVER_ID                    INTEGER         NOT NULL ,    /* 수신자 ID  */
    RECEIVER_NAME                  VARCHAR(200)             ,    /* 수신자 이름  */
    RECEIVER_DESC                  VARCHAR(100)    NOT NULL ,    /* 수신자 설명  */
    RECV_BY_EMAIL                  VARCHAR(1)      NOT NULL ,    /* EMAIL 로 수신? (0, 1) */
    EMAIL_ADDR                     VARCHAR(100)             ,    /* EMAIL 주소  */
    RECV_BY_SMS                    VARCHAR(1)      NOT NULL ,    /* 핸드폰 으로 수신? (0, 1) */
    SMS_NUM                        VARCHAR(20)              ,    /* 핸드폰 번호  */
    RECV_BY_MESSENGER              VARCHAR(1)      NOT NULL ,    /* 메신저로 수신? (0, 1) */
    MESSENGER_ID                   VARCHAR(50)              ,    /* 메신저 ID  */
    RECV_BY_TERMINAL               VARCHAR(1)      NOT NULL ,    /* 단말로 수신? (0, 1) */
    TERMINAL_ID                    VARCHAR(50)              ,    /* 단말 ID  */
    RECV_BY_DEV1                   VARCHAR(1)               ,    /* DEV1 장치로 수신? (0, 1) */
    DEV1_POINT                     VARCHAR(50)              ,    /* DEV1 장치 주소 CUSTOMIZE 영역 */
    RECV_BY_DEV2                   VARCHAR(1)               ,    /* DEV2 장치로 수신? (0, 1) */
    DEV2_POINT                     VARCHAR(50)              ,    /* DEV2 장치 주소 CUSTOMIZE 영역 */
    RECV_BY_DEV3                   VARCHAR(1)               ,    /* DEV3 장치로 수신? (0, 1) */
    DEV3_POINT                     VARCHAR(50)              ,    /* DEV3 장치 주소 CUSTOMIZE 영역 */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종수정일  */
    CONSTRAINT NBS_NOTIFY_RECEIVER_PK PRIMARY KEY (RECEIVER_ID)
);


/* Job 통지 대상 내역*/
CREATE SEQUENCE NBS_NOTIFY_SEND_LIST_SEQ START WITH 1;
CREATE TABLE NBS_NOTIFY_SEND_LIST (
    SEQ_NO                         INTEGER         NOT NULL ,    /*   */
    JOB_EXECUTION_ID               VARCHAR(100)    NOT NULL ,    /*   */
    JOB_ID                         VARCHAR(100)    NOT NULL ,    /*   */
    JOB_DESC                       VARCHAR(300)             ,    /*   */
    AGENT_NODE                     VARCHAR(50)     NOT NULL ,    /* agent 의 system.id  */
    RETURN_CODE                    INTEGER                  ,    /*   */
    ERROR_MSG                      VARCHAR(1000)            ,    /*   */
    RECEIVER_ID                    INTEGER         NOT NULL ,    /* 수신자 ID  */
    RECEIVER_NAME                  VARCHAR(200)    NOT NULL ,    /* 수신자 ID  */
    RECV_TYPE                      VARCHAR(20)     NOT NULL ,    /* 수신 매체 타입 SMS', 'EMAIL', 'TERMINAL', 'DEV1', 'DEV2', 'DEV3' */
    RECV_POINT                     VARCHAR(50)              ,    /* 수신 매체 주소 SMS번호, EMAIL 주소, 등등 */
    CREATE_TIME                    VARCHAR(20)     NOT NULL ,    /* notify 생성 시각  */
    SEND_STATE                     VARCHAR(2)      NOT NULL ,    /*  I':초기, 'S':성공, 'F':에러, 'X':garbage */
    SEND_TIME                      VARCHAR(20)              ,    /* 전송 시각  */
    TRY_COUNT                      INTEGER                  ,    /* 시도 횟수  */
    NOTIFY_ID                      INTEGER                  ,    /*   */
    CHECK_VALUE1                   VARCHAR(20)              ,    /*   */
    CHECK_VALUE2                   VARCHAR(20)              ,    /*   */
    CHECK_VALUE3                   VARCHAR(20)              ,    /*  EO':정상종료시, 'EF':에러종료시, 'LONGRUN':장기수행 */
    PROC_SYSTEM_ID                 VARCHAR(100)             ,    /* 전송 처리 스케줄러 system.id  */
    CONSTRAINT NBS_NOTIFY_SEND_LIST_PK PRIMARY KEY (SEQ_NO)
);
CREATE INDEX NBS_NOTIFY_SEND_LIST_IDX1 ON NBS_NOTIFY_SEND_LIST(JOB_EXECUTION_ID);


/* */
CREATE TABLE NBS_USER (
    USER_ID                        VARCHAR(50)     NOT NULL ,    /* 사용자 ID  */
    USER_PASSWD                    VARCHAR(50)     NOT NULL ,    /* 사용자 비밀번호  */
    USER_NAME                      VARCHAR(100)    NOT NULL ,    /* 사용자 이름  */
    USER_DESC                      VARCHAR(100)             ,    /* 수신자 설명  */
    TEAM1                          VARCHAR(50)              ,    /* 팀명  */
    TEAM2                          VARCHAR(50)              ,    /* 서브 팀명  */
    EMAIL                          VARCHAR(200)             ,    /* EMAIL  */
    PHONE                          VARCHAR(20)              ,    /*   */
    IS_ADMIN                       VARCHAR(1)      NOT NULL ,    /* 관리자 여부 '1', '0' */
    IS_OPERATOR                    VARCHAR(1)      NOT NULL ,    /* 운영자 여부 '1', '0' JOB CONTROL, MODIFY 권한 */
    IS_ACTIVE                      VARCHAR(1)      NOT NULL ,    /* 유효여부 '1', '0' */
    OPER_JOB_ID_EXP                VARCHAR(100)             ,    /* OPERATOR 인 경우, 허용되는 Job Id 패턴  */
    CREATE_TIME                    VARCHAR(20)     NOT NULL ,    /* 등록일  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종수정일  */
    CONSTRAINT NBS_USER_PK PRIMARY KEY (USER_ID)
);
CREATE INDEX NBS_USER_IDX1 ON NBS_USER(USER_NAME);


/* ID 채번 테이블*/
CREATE TABLE NBS_IDGEN_BASE (
    ID_TYPE                        VARCHAR(10)     NOT NULL ,    /*   */
    KEY_NAME                       VARCHAR(100)    NOT NULL ,    /*   */
    LAST_SEQ                       INTEGER         NOT NULL ,    /*   */
    LAST_MODIFY_USER               VARCHAR(100)    NOT NULL ,    /*   */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /*   */
    CONSTRAINT NBS_IDGEN_BASE_PK PRIMARY KEY (ID_TYPE,KEY_NAME)
);


/* TimeScheduler 로그 테이블*/
CREATE TABLE NBS_TIMESCH_LOG (
    TS_DATE                        VARCHAR(8)      NOT NULL ,    /* SHOULD BE DB TIME  */
    TS_TIME                        VARCHAR(4)      NOT NULL ,    /* SHOULD BE DB TIME  */
    SYSTEM_ID                      VARCHAR(100)    NOT NULL ,    /*   */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /*   */
    CONSTRAINT NBS_TIMESCH_LOG_PK PRIMARY KEY (TS_DATE,TS_TIME)
);


/* OLD JOB CLEAN 로그 테이블*/
CREATE TABLE NBS_CLEAN_LOG (
    RUN_DATE                       VARCHAR(8)      NOT NULL ,    /* 작업일자  */
    DEL_BASE_DATE                  VARCHAR(8)      NOT NULL ,    /* 삭제대상 기준일  */
    SYSTEM_ID                      VARCHAR(100)    NOT NULL ,    /*   */
    JOB_INS_CNT                    INTEGER                  ,    /* NBS_JOB_INS 삭제 건수  */
    JOB_INS_OBJ_CNT                INTEGER                  ,    /* NBS_JOB_INS_OBJ_STORE 삭제 건수  */
    JOB_INS_PREJOB_CNT             INTEGER                  ,    /* NBS_JOB_INS_PREJOB 삭제 건수  */
    JOB_EXE_CNT                    INTEGER                  ,    /* NBS_JOB_EXE 삭제 건수  */
    JOB_EXE_OBJ_CNT                INTEGER                  ,    /* NBS_JOB_EXE_OBJ_STORE 삭제 건수  */
    UPLOAD_TEMP_FILE_CNT           INTEGER                  ,    /* 업로드용 임시 파일 삭제 건수  */
    TIMESCH_LOG_CNT                INTEGER                  ,    /* NBS_TIMESCH_LOG 테이블 삭제 건수  */
    IDGEN_BASE_CNT                 INTEGER                  ,    /* NBS_IDGEN_BASE  테이블 삭제 건수  */
    CLEAN_START_TIME               VARCHAR(20)     NOT NULL ,    /*   */
    CLEAN_END_TIME                 VARCHAR(20)              ,    /*   */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /*   */
    CONSTRAINT NBS_CLEAN_LOG_PK PRIMARY KEY (RUN_DATE)
);


/* Job 그룹 속성 정의 테이블*/
CREATE TABLE NBS_JOBGROUP_ATTR_DEF (
    ATTR_ID                        VARCHAR(100)    NOT NULL ,    /* 속성 ID  */
    ATTR_NAME                      VARCHAR(300)    NOT NULL ,    /* 속성 표시명  */
    ATTR_DESC                      VARCHAR(100)             ,    /* 속성 설명  */
    VALUE_TYPE                     VARCHAR(50)     NOT NULL ,    /* 값 타입 (TEXT, TEXTAREA, LIST) */
    VALUE_CHECK                    VARCHAR(300)             ,    /* 유효값 범위 (최대길이/List 등) */
    DISPLAY_LINE                   INTEGER         NOT NULL ,    /* 표시 라인수  */
    DISPLAY_MONITOR                VARCHAR(1)      NOT NULL ,    /* 그룹 모니터링에 표시 여부 BOOL */
    DISPLAY_ORDER                  INTEGER         NOT NULL ,    /* 표시 순서  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 일시  */
    CONSTRAINT NBS_JOBGROUP_ATTR_DEF_PK PRIMARY KEY (ATTR_ID)
);


/* Job 그룹 기본 테이블*/
CREATE TABLE NBS_JOBGROUP (
    GROUP_ID                       VARCHAR(100)    NOT NULL ,    /* 그룹 ID  */
    GROUP_NAME                     VARCHAR(100)    NOT NULL ,    /* 그룹 표시명  */
    GROUP_DESC                     VARCHAR(300)             ,    /* 그룹 설명  */
    PARENT_ID                      VARCHAR(100)    NOT NULL ,    /* 부모 그룹 ID  */
    CREATOR_ID                     VARCHAR(50)     NOT NULL ,    /* 생성자 ID  */
    OWNER_ID                       VARCHAR(50)     NOT NULL ,    /* 담당자 ID  */
    CREATE_TIME                    VARCHAR(20)     NOT NULL ,    /* 생성 일시  */
    LAST_MODIFY_TIME               VARCHAR(20)     NOT NULL ,    /* 최종 변경 일시  */
    CONSTRAINT NBS_JOBGROUP_PK PRIMARY KEY (GROUP_ID)
);


/* Job 그룹 속성 값 테이블*/
CREATE TABLE NBS_JOBGROUP_ATTR (
    GROUP_ID                       VARCHAR(100)    NOT NULL ,    /* 그룹 ID  */
    ATTR_ID                        VARCHAR(100)    NOT NULL ,    /* 속성 ID  */
    ATTR_VALUE                     VARCHAR(1000)            ,    /* 속성 값  */
    CONSTRAINT NBS_JOBGROUP_ATTR_PK PRIMARY KEY (GROUP_ID,ATTR_ID)
);


/* 사용자별 권한 설정 화면 Job 그룹의 운영,조회 권한 설정 테이블*/
CREATE TABLE NBS_USER_AUTH (
    USER_ID                        VARCHAR(50)     NOT NULL ,    /* 사용자 ID  */
    AUTH_KIND                      VARCHAR(50)     NOT NULL ,    /* 권한 종류 ['VIEW_JOBGROUP', 'OPER_JOBGROUP', ...) */
    TARGET_OBJECT                  VARCHAR(100)    NOT NULL ,    /* 대상 객체 [JobGroupId, ...) */
    CONSTRAINT NBS_USER_AUTH_PK PRIMARY KEY (USER_ID,AUTH_KIND,TARGET_OBJECT)
);


