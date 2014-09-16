#! /usr/bin/groovy

@GrabResolver(name='nexus', root='http://bandt.si.cnr.it:8280/nexus/content/groups/public/')

@Grab(group='it.cnr.si.cool', module='cool-common-core', version='1.3')
@Grab(group='org.springframework', module='org.springframework.test', version='3.1.2.RELEASE')
@Grab(group='javax.jms', module='jms', version='1.1')

import org.springframework.context.support.ClassPathXmlApplicationContext

def context = new ClassPathXmlApplicationContext('META-INF/cool-common-core-groovy-context.xml')

println context.getBean('sample').id

