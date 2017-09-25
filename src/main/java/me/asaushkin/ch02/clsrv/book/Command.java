package me.asaushkin.ch02.clsrv.book;

import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract class that includes the necessary elements of every Command
 * @author author
 *
 */
public abstract class Command {

	/**
	 * String with all the data of the command: The command itself and its parameters
	 */
	protected String[] command;
	
	/**
	 * Boolean value to indicate that the command is cacheable or not
	 */
	private boolean cacheable;
	
	/**
	 * Constructor of the class
	 * @param command
	 */
	public Command (String [] command) {
		this.command=command;
		setCacheable(true);
	}
	
	/**
	 * Abstract method that executes the command
	 * @return An String with the response of the command
	 */
	public abstract String execute () throws URISyntaxException;

	public boolean isCacheable() {
		return cacheable;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	/**
     * Class that implements the serial version of the Query Command. The format of
     * this query is: q;codCountry;codIndicator;year where codCountry is the code of the country,
     * codIndicator is the code of the indicator and the year is an optional parameter with the year
     * you want to query
     * @author author
     *
     */
    public static class QueryCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public QueryCommand (String [] command) {
            super(command);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() throws URISyntaxException {

            WDIDAO dao=WDIDAO.getDAO();


            if (command.length==3) {
                return dao.query(command[1], command[2]);
            } else if (command.length==4) {
                try {
                    return dao.query(command[1], command[2], Short.parseShort(command[3]));
                } catch (Exception e) {
                    return "ERROR;Bad Command";
                }
            } else {
                return "ERROR;Bad Command";
            }
        }

    }

	/**
     * Class that implements the serial version of the Error command. It's executed
     * when an unknown command arrives
     * @author author
     *
     */
    public static class ErrorCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ErrorCommand(String[] command) {
            super(command);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() {
            return "Unknown command: "+command[0];
        }

    }

	/**
     * Class that implements the serial version of the Stop command.
     * Finish the execution of the server
     * @author author
     *
     */
    public static class StopCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public StopCommand (String [] command) {
            super (command);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() {
            return "Server stopped";
        }

    }

	/**
     * Class that implements the serial version of the Report command.
     * Report: The format of this query is: r:codIndicator where codIndicator
     * is the code of the indicator you want to report
     * @author author
     *
     */
    public static class ReportCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ReportCommand (String [] command) {
            super(command);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() throws URISyntaxException {

            WDIDAO dao=WDIDAO.getDAO();
            return dao.report(command[1]);
        }

    }

	/**
     * Concurrent version of the ErrorCommand. It's executed when an unknown command arrives
     * @author author
     *
     */
    public static class ConcurrentErrorCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ConcurrentErrorCommand(String[] command) {
            super(command);
            setCacheable(false);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() {
            return "Unknown command: "+command[0];
        }

    }

	/**
     * Class that implement the concurrent version of the status command.
     * Returns information about the executor that executes the concurrent tasks of the
     * server
     * @author author
     *
     */
    public static class ConcurrentStatusCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ConcurrentStatusCommand (String[] command) {
            super(command);
            setCacheable(false);
        }


        @Override
        /**
         * Method that executes the command
         */
        public String execute() {
            StringBuilder sb=new StringBuilder();
            ThreadPoolExecutor executor=ConcurrentServer.getExecutor();

            sb.append("Server Status;");
            sb.append("Actived Threads: ");
            sb.append(String.valueOf(executor.getActiveCount()));
            sb.append(";");
            sb.append("Maximum Pool Size: ");
            sb.append(String.valueOf(executor.getMaximumPoolSize()));
            sb.append(";");
            sb.append("Core Pool Size: ");
            sb.append(String.valueOf(executor.getCorePoolSize()));
            sb.append(";");
            sb.append("Pool Size: ");
            sb.append(String.valueOf(executor.getPoolSize()));
            sb.append(";");
            sb.append("Largest Pool Size: ");
            sb.append(String.valueOf(executor.getLargestPoolSize()));
            sb.append(";");
            sb.append("Completed Task Count: ");
            sb.append(String.valueOf(executor.getCompletedTaskCount()));
            sb.append(";");
            sb.append("Task Count: ");
            sb.append(String.valueOf(executor.getTaskCount()));
            sb.append(";");
            sb.append("Queue Size: ");
            sb.append(String.valueOf(executor.getQueue().size()));
            sb.append(";");
            sb.append("Cache Size: ");
            sb.append(String.valueOf(ConcurrentServer.getCache().getItemCount()));
            sb.append(";");
            Logger.sendMessage(sb.toString());
            return sb.toString();
        }

    }

	/**
     * Class that implements the concurrent version of the Query Command. The format of
     * this query is: q;codCountry;codIndicator;year where codCountry is the code of the country,
     * codIndicator is the code of the indicator and the year is an optional parameter with the year
     * you want to query
     * @author author
     *
     */
    public static class ConcurrentQueryCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ConcurrentQueryCommand (String [] command) {
            super(command);
        }

        @Override
        /**
         * Method that executes the query
         */
        public String execute() throws URISyntaxException {

            WDIDAO dao=WDIDAO.getDAO();

            if (command.length==3) {
                return dao.query(command[1], command[2]);
            } else if (command.length==4) {
                try {
                    return dao.query(command[1], command[2], Short.parseShort(command[3]));
                } catch (Exception e) {
                    return "ERROR;Bad Command";
                }
            } else {
                return "ERROR;Bad Command";
            }
        }

    }

	/**
     * Class that implements the concurrent version of the Stop command.
     * Stops the server
     * @author author
     *
     */
    public static class ConcurrentStopCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ConcurrentStopCommand (String [] command) {
            super (command);
            setCacheable(false);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() {
            ConcurrentServer.shutdown();
            return "Server stopped";
        }

    }

	/**
     * Class that implements the concurrent version of the Report command.
     * Report: The format of this query is: r:codIndicator where codIndicator
     * is the code of the indicator you want to report
     * @author author
     *
     */
    public static class ConcurrentReportCommand extends Command {

        /**
         * Constructor of the class
         * @param command String that represents the command
         */
        public ConcurrentReportCommand (String [] command) {
            super(command);
        }

        @Override
        /**
         * Method that executes the command
         */
        public String execute() throws URISyntaxException {

            WDIDAO dao=WDIDAO.getDAO();
            return dao.report(command[1]);
        }

    }
}
