package mcl;

import java.util.ArrayList;
import java.util.List;

import util.gui.RobotController;
import util.gui.VariablesController;
import util.gui.Window;
import util.robot.Pose;
import util.robot.RobotState;

import com.beust.jcommander.JCommander;

public class Main {
	
	public static void main(String[] args) throws Throwable{
		//for debug mode
		if(args.length==0){
			String[] targs = {/*"-cl",*/
					//"-i","file:///Users/ihsumlee/Jolly/jpg/sim_map.jpg"
					"-i","file:///home/wuser/backup/jpg/sim_map.jpg"
					,"-o","36"
					,"-rl","true"
//							,"-rx","30"
//							,"-ry","30"
					,"-p","10"
//							,"-cl"
					,"-D","false"
					,"-c","true"
					};
			args = targs;
		}
		
		/**
		 * First step:
		 * to create the localization algorithm
		 * and setup the listener for SAMCL
		 */
		//TODO parameter 
		final MCL mcl = new MCL(false,
				18, //orientation
				//"file:///home/w514/map.jpg",//map image file
				"hdfs:///user/eeuser/map1024.jpeg",
				(float) 0.005, //delta energy
				100, //total particle
				(float) 0.001, //threshold xi
				(float) 0.6, //rate of population
				10);//competitive strength
		JCommander jc = new JCommander();
		jc.setAcceptUnknownOptions(true);
		jc.addObject(mcl);
		jc.parse(args);
		if(mcl.help){
			jc.usage();
			System.exit(0);
		}
		mcl.setup();
//				if(!mcl.onCloud){
//					System.out.println("start to pre-caching");
//					mcl.Pre_caching();
//				}	
		
		/**
		 * Second step:
		 * to create a robot
		 * setup the listener of Robot
		 * */
		List<Pose> path = new ArrayList<Pose>();
		path.add(new Pose(400,150,0));
		path.add(new Pose(400,150,90));
		path.add(new Pose(400,400,90));
		path.add(new Pose(400,400,180));
		path.add(new Pose(350,400,180));
		path.add(new Pose(350,400,90));
		path.add(new Pose(350,550,90));
		path.add(new Pose(350,550,180));
		path.add(new Pose(150,550,180));
		path.add(new Pose(150,550,270));
		path.add(new Pose(150,150,270));
		path.add(new Pose(150,150,0));
		RobotState robot = new RobotState(150, 150, 0, /*null*/mcl.grid, mcl.tableName, path);
		jc = new JCommander();
		jc.setAcceptUnknownOptions(true);
		jc.addObject(robot);
		jc.parse(args);
		//TODO setup robot
		robot.setInitModel(robot.getUt());
		robot.setInitPose(robot.getPose());
		@SuppressWarnings("unused")
		RobotController robotController = new RobotController("robot controller", robot,mcl);
		VariablesController vc = new VariablesController(mcl);
		Thread t = new Thread(robot);
		t.start();
		/**
		 * Third step:
		 * start to run samcl
		 */
		//TODO WINDOW
		Window window = new Window("mcl image", mcl,robot);
		
		//TODO test 2014/06/19
		int counter = 0;
		while(true){
			counter++;
			window.setTitle("mcl image:"+String.valueOf(counter));
			robot.goStraight();
			mcl.run(robot, window);
			robot.lock();
			robot.initRobot();
		}	
	}
	
}