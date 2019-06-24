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

package it.cnr.cool.service.frontOffice;

import com.google.gson.JsonParser;
import it.cnr.cool.MainTestContext;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={MainTestContext.class})
public class LogTest {
	@Autowired
	private FrontOfficeService frontOfficeService;
    private  TestAppender testAppender;
    private com.google.gson.JsonObject jsonStackTrace;
    private JsonParser jsParser = new JsonParser();



    @Before
	public void createLog() throws ParseException {
		int codice = 1;
		String application = "cool-jconon";
		String afterString = "2013-07-09";
		String stackTrace = "{\"codice\":"
				+ codice
				+ ",\"mappa\":{\"user\":\"admin\",\"url\":\"/doccnr/logger\", \"application\":\""
				+ application
				+ "\"}, \"testo\":\"Ajax request has employed 1770 msec\",\"user-agent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17\",\"Date\":\""
				+ afterString + "\",\"IP\":\"127.0.0.1\"}";

        jsonStackTrace = jsParser.parse(stackTrace).getAsJsonObject();
        testAppender = new TestAppender();
        Logger.getRootLogger().addAppender(testAppender);
		Map<String, Object> mapPost = frontOfficeService.post(null, null, null, null, TypeDocument.Log, stackTrace);
    }


    @Test
    public void testGetLoggerInConsole() throws IOException{
        LoggingEvent loggingEvent = testAppender.events.get(0);
        assertTrue("Appender delle log vuoto",testAppender.events.size()==1);

        assertEquals("Livello del delle log SBAGLIATO!", Level.ERROR.toString(),loggingEvent.getLevel().toString());
        com.google.gson.JsonObject console = jsParser.parse(loggingEvent.getMessage().toString().substring(4)).getAsJsonObject();

        assertEquals("Campo \"codice\" DIVERSO!", jsonStackTrace.get("codice").getAsInt(), console.get("codice").getAsInt());
        assertEquals("campo \"testo\" DIVERSO!", jsonStackTrace.get("testo").getAsString(), console.get("testo").getAsString());

        com.google.gson.JsonObject mappaConsole = console.get("mappa").getAsJsonObject();
        com.google.gson.JsonObject mappaStacktrace = jsonStackTrace.get("mappa").getAsJsonObject();

        assertEquals("Campo \"user\" DIVERSO!", mappaStacktrace.get("user"), mappaConsole.get("user"));
        assertEquals("Campo \"url\" DIVERSO!", mappaStacktrace.get("url"), mappaConsole.get("url"));
        assertEquals("Compo \"application\" DIVERSO!", mappaStacktrace.get("application"), mappaConsole.get("application"));
    }

    public static class TestAppender extends AppenderSkeleton {
        public List<LoggingEvent> events = new ArrayList<LoggingEvent>();
        public void close() {}
        public boolean requiresLayout() {return false;}
        @Override
        protected void append(LoggingEvent event) {
            events.add(event);
        }
    }
}