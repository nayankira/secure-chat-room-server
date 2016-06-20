package chatserver;


import org.kohsuke.args4j.Option;

/**
 * Class with command line options.
 * 
 * @author Adel
 *
 */
public class CommandLineValues {
		@Option(required = false, name = "", aliases ={}, usage="Host Address")
		private String host;
		
		// Give it a default value of 4444 sec
		@Option(required = false, name = "-p", aliases = {"--port"}, usage="Port Address")
		private int port = 4444;

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}
	}

