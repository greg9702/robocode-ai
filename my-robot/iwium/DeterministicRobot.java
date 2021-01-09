package iwium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class DeterministicRobot extends AdvancedRobot {
    private static final Logger m_logger = LogManager.getLogger("hunterbase");

    protected double m_xpos = 0;

    public void run() {
        turnLeft(getHeading() - 90);
        while (true) {
            turnLeft(getHeading() - 90);

//            setTurnRadarRight(Double.POSITIVE_INFINITY);
//            if (m_xpos < getBattleFieldWidth() * 0.2) {
//                turnLeft(getHeading() - 90);
//            }
//            else if (m_xpos > getBattleFieldWidth()*0.8) {
//                m_logger.debug("XD");
//                turnLeft(getHeading() + 90);
//            }
//            setAhead(Double.POSITIVE_INFINITY);
//            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        turnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
//        if (getGunHeat() == 0) {
//            fire(3);
//        }
    }

    /**
     * What to do on every status (tick) update.
     */
    public void onStatus(StatusEvent e) {
        RobotStatus s = e.getStatus();
        m_xpos = s.getX();
    }
}