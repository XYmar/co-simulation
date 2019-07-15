/*初始化角色*/
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'admin','系统管理员',SYSDATE ,0 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'security_guard','安全保密员',SYSDATE,0 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'security_auditor','安全审计员',SYSDATE,0 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'project_manager','项目管理员',SYSDATE,1 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'file_auditor','仿真技术文件审核员',SYSDATE,1 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'normal_designer','一般仿真设计员',SYSDATE,1 ) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into ROLE(ID,NAME,DESCRIPTION,CREATE_TIME,CHANGEABLE) values (sys_guid(),'users','普通用户',SYSDATE,1 ) select 'admin' from dual;
/*初始化库*/
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME) values (sys_guid(),'模型库','模型库',SYSDATE) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME) values (sys_guid(),'参数库','参数库',SYSDATE) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME) values (sys_guid(),'仿真库','仿真库',SYSDATE) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME) values (sys_guid(),'知识库','知识库',SYSDATE) select 'admin' from dual;
/*初始化18个子库*/
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'电路印制模板库','电路印制模板库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'结构体库','结构体库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'工装设备库','工装设备库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'元器件库','元器件库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'部组件库','部组件库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'整体装配库','整体装配库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '模型库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'芯片参数库','芯片参数库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '参数库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'材料属性库','材料属性库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '参数库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'仿真参数库','仿真参数库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '参数库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'试验数据库','试验数据库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '参数库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'电磁场仿真库','电磁场仿真库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '仿真库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'热力学仿真库','热力学仿真库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '仿真库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'装配仿真库','装配仿真库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '仿真库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'电路仿真库','电路仿真库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '仿真库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'联合仿真库','联合仿真库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '仿真库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'仿真模板库','仿真模板库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '知识库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'仿真经验库','仿真经验库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '知识库')) select 'admin' from dual;
insert when (NOT EXISTS (select * from USERS WHERE USERS.username = 'admin')) then into SUB_DEPOT(ID,TYPE,DESCRIPTION,CREATE_TIME,DEPOT_ID) values (sys_guid(),'仿真标准库','仿真标准库',SYSDATE,(SELECT ID FROM DEPOT WHERE DEPOT.TYPE = '知识库')) select 'admin' from dual;



