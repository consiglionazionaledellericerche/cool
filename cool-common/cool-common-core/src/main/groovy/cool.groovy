/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#! /usr/bin/groovy

@GrabResolver(name='nexus', root='http://bandt.si.cnr.it:8280/nexus/content/groups/public/')

@Grab(group='it.cnr.si.cool', module='cool-common-core', version='1.3')
@Grab(group='org.springframework', module='org.springframework.test', version='3.1.2.RELEASE')
@Grab(group='javax.jms', module='jms', version='1.1')

import org.springframework.context.support.ClassPathXmlApplicationContext

def context = new ClassPathXmlApplicationContext('META-INF/cool-common-core-groovy-context.xml')

println context.getBean('sample').id

