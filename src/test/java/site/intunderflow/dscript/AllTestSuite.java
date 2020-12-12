package site.intunderflow.dscript;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import site.intunderflow.dscript.onstart.OnStart;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({ "**/*Test.class" })
public class AllTestSuite {

    @BeforeClass
    public static void setup(){
        OnStart.onStart();
    }

}