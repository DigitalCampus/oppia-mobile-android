package suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import UI.LoginUITest;
import UI.RegisterUITest;
import UI.ResetUITest;
import UI.WelcomeUITest;

/**
 * Created by Alberto on 28/06/2016.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({WelcomeUITest.class, LoginUITest.class, RegisterUITest.class, ResetUITest.class})
public class MainActivitySuite {
}
