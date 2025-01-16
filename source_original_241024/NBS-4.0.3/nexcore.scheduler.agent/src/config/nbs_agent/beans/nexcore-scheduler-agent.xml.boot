<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:util="http://www.springframework.org/schema/util"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xmlns:tx="http://www.springframework.org/schema/tx"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd
            http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
            http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!-- ################################################################## -->
    <!-- nc_bat_agent 에서만 사용함. properties 파일 초기화를 여기서 하며, WAS, Lib 방식에서는 다른 xml 에서 이 작업을 한다. -->
    <!-- ################################################################## -->
    <bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <description>
            <![CDATA[Spring(3.x이상) context 초기화시 임의로 properties를 지정하는 기능.]]>
        </description>
		<property name="systemPropertiesModeName"       value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/>
		<property name="ignoreUnresolvablePlaceholders" value="true"/>
	    <property name="ignoreResourceNotFound"         value="true"/>
	    <property name="fileEncoding"                   value="UTF-8"/>
		<property name="locations">
		   	<bean class="org.springframework.beans.factory.config.ListFactoryBean">
		    	<property name="sourceList">
					<list>
		                <value>properties/*.properties</value>
					</list>
		    	</property>
			</bean>
		</property>
	</bean>
</beans>
