package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _mycolors = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _mylogo = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _myconame = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _mystamp = null;
public b4a.example.card _card = null;
public static int _coffeecount = 0;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _mybuttoncolours = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnscan = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imglogo = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp1 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp2 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp3 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp4 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp5 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgstamp6 = null;
public anywheresoftware.b4a.objects.LabelWrapper _lblcompanyname = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlbg = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp1 = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp2 = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp3 = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp4 = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp5 = null;
public anywheresoftware.b4a.objects.PanelWrapper _pnlstamp6 = null;
public b4a.example.coffeetheme _mytheme = null;
public static boolean _scansuccess = false;
public ice.zxing.b4aZXingLib _qrscanner = null;
public anywheresoftware.b4a.objects.ButtonWrapper _no = null;
public anywheresoftware.b4a.objects.ButtonWrapper _yes = null;
public b4a.example.themecalc _themecalc = null;
  public Object[] GetGlobals() {
		return new Object[] {"Activity",mostCurrent._activity,"btnScan",mostCurrent._btnscan,"Card",Debug.moduleToString(b4a.example.card.class),"CoffeeCount",_coffeecount,"imgLogo",mostCurrent._imglogo,"imgStamp1",mostCurrent._imgstamp1,"imgStamp2",mostCurrent._imgstamp2,"imgStamp3",mostCurrent._imgstamp3,"imgStamp4",mostCurrent._imgstamp4,"imgStamp5",mostCurrent._imgstamp5,"imgStamp6",mostCurrent._imgstamp6,"lblCompanyName",mostCurrent._lblcompanyname,"myButtonColours",_mybuttoncolours,"myColors",_mycolors,"myCoName",_myconame,"myLogo",_mylogo,"myStamp",_mystamp,"myTheme",mostCurrent._mytheme,"No",mostCurrent._no,"pnlBG",mostCurrent._pnlbg,"pnlStamp1",mostCurrent._pnlstamp1,"pnlStamp2",mostCurrent._pnlstamp2,"pnlStamp3",mostCurrent._pnlstamp3,"pnlStamp4",mostCurrent._pnlstamp4,"pnlStamp5",mostCurrent._pnlstamp5,"pnlStamp6",mostCurrent._pnlstamp6,"qrscanner",mostCurrent._qrscanner,"scanSuccess",_scansuccess,"ThemeCalc",Debug.moduleToString(b4a.example.themecalc.class),"Yes",mostCurrent._yes};
}

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}

public static void killProgram() {
     {
            Activity __a = null;
            if (main.previousOne != null) {
				__a = main.previousOne.get();
			}
            else {
                BA ba = main.mostCurrent.processBA.sharedProcessBA.activityBA.get();
                if (ba != null) __a = ba.activity;
            }
            if (__a != null)
				__a.finish();}

}
public static String  _activity_create(boolean _firsttime) throws Exception{
try {
		Debug.PushSubsStack("Activity_Create (main) ","main",0,mostCurrent.activityBA,mostCurrent,51);
Debug.locals.put("FirstTime", _firsttime);
 BA.debugLineNum = 51;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
Debug.ShouldStop(262144);
 BA.debugLineNum = 53;BA.debugLine="Activity.LoadLayout(\"main\")";
Debug.ShouldStop(1048576);
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 BA.debugLineNum = 55;BA.debugLine="myTheme.Initialize ' initialise theme database on";
Debug.ShouldStop(4194304);
mostCurrent._mytheme._initialize(processBA);
 BA.debugLineNum = 56;BA.debugLine="loadDBcolours";
Debug.ShouldStop(8388608);
_loaddbcolours();
 BA.debugLineNum = 57;BA.debugLine="loadDBlogo";
Debug.ShouldStop(16777216);
_loaddblogo();
 BA.debugLineNum = 58;BA.debugLine="loadDBname";
Debug.ShouldStop(33554432);
_loaddbname();
 BA.debugLineNum = 59;BA.debugLine="loadStamp";
Debug.ShouldStop(67108864);
_loadstamp();
 BA.debugLineNum = 60;BA.debugLine="loadDBbuttonColours";
Debug.ShouldStop(134217728);
_loaddbbuttoncolours();
 BA.debugLineNum = 62;BA.debugLine="CoffeeCount=5";
Debug.ShouldStop(536870912);
_coffeecount = (int) (5);
 BA.debugLineNum = 65;BA.debugLine="End Sub";
Debug.ShouldStop(1);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _activity_pause(boolean _userclosed) throws Exception{
try {
		Debug.PushSubsStack("Activity_Pause (main) ","main",0,mostCurrent.activityBA,mostCurrent,127);
Debug.locals.put("UserClosed", _userclosed);
 BA.debugLineNum = 127;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
Debug.ShouldStop(1073741824);
 BA.debugLineNum = 129;BA.debugLine="End Sub";
Debug.ShouldStop(1);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _activity_resume() throws Exception{
try {
		Debug.PushSubsStack("Activity_Resume (main) ","main",0,mostCurrent.activityBA,mostCurrent,123);
 BA.debugLineNum = 123;BA.debugLine="Sub Activity_Resume";
Debug.ShouldStop(67108864);
 BA.debugLineNum = 125;BA.debugLine="End Sub";
Debug.ShouldStop(268435456);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _btnno_click() throws Exception{
try {
		Debug.PushSubsStack("btnno_Click (main) ","main",0,mostCurrent.activityBA,mostCurrent,209);
 BA.debugLineNum = 209;BA.debugLine="Sub btnno_Click ' you can also save and redeem lat";
Debug.ShouldStop(65536);
 BA.debugLineNum = 210;BA.debugLine="activity.LoadLayout(\"main\") 'will return to main";
Debug.ShouldStop(131072);
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 BA.debugLineNum = 211;BA.debugLine="End Sub";
Debug.ShouldStop(262144);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _btnscan_click() throws Exception{
try {
		Debug.PushSubsStack("btnScan_Click (main) ","main",0,mostCurrent.activityBA,mostCurrent,200);
 BA.debugLineNum = 200;BA.debugLine="Sub btnScan_Click 'in order to bring information a";
Debug.ShouldStop(128);
 BA.debugLineNum = 201;BA.debugLine="updateStamps";
Debug.ShouldStop(256);
_updatestamps();
 BA.debugLineNum = 202;BA.debugLine="End Sub";
Debug.ShouldStop(512);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _btnscan_longclick() throws Exception{
try {
		Debug.PushSubsStack("btnScan_LongClick (main) ","main",0,mostCurrent.activityBA,mostCurrent,213);
 BA.debugLineNum = 213;BA.debugLine="Sub btnScan_LongClick ' in order to scan the barco";
Debug.ShouldStop(1048576);
 BA.debugLineNum = 214;BA.debugLine="qrscanner.isportrait = True";
Debug.ShouldStop(2097152);
mostCurrent._qrscanner.isportrait = anywheresoftware.b4a.keywords.Common.True;
 BA.debugLineNum = 215;BA.debugLine="qrscanner.useFrontCam = False";
Debug.ShouldStop(4194304);
mostCurrent._qrscanner.useFrontCam = anywheresoftware.b4a.keywords.Common.False;
 BA.debugLineNum = 218;BA.debugLine="qrscanner.timeoutDuration = 30";
Debug.ShouldStop(33554432);
mostCurrent._qrscanner.timeoutDuration = (int) (30);
 BA.debugLineNum = 222;BA.debugLine="qrscanner.theViewFinderXfactor = 0.7";
Debug.ShouldStop(536870912);
mostCurrent._qrscanner.theViewFinderXfactor = 0.7;
 BA.debugLineNum = 223;BA.debugLine="qrscanner.theViewFinderYfactor = 0.5";
Debug.ShouldStop(1073741824);
mostCurrent._qrscanner.theViewFinderYfactor = 0.5;
 BA.debugLineNum = 225;BA.debugLine="qrscanner.theFrameColor = Colors.LightGray";
Debug.ShouldStop(1);
mostCurrent._qrscanner.theFrameColor = anywheresoftware.b4a.keywords.Common.Colors.LightGray;
 BA.debugLineNum = 226;BA.debugLine="qrscanner.theLaserColor = Colors.Red";
Debug.ShouldStop(2);
mostCurrent._qrscanner.theLaserColor = anywheresoftware.b4a.keywords.Common.Colors.Red;
 BA.debugLineNum = 227;BA.debugLine="qrscanner.theMaskColor = Colors.argb(95, 0, 0, 25";
Debug.ShouldStop(4);
mostCurrent._qrscanner.theMaskColor = anywheresoftware.b4a.keywords.Common.Colors.ARGB((int) (95),(int) (0),(int) (0),(int) (255));
 BA.debugLineNum = 228;BA.debugLine="qrscanner.theResultColor = Colors.Green";
Debug.ShouldStop(8);
mostCurrent._qrscanner.theResultColor = anywheresoftware.b4a.keywords.Common.Colors.Green;
 BA.debugLineNum = 229;BA.debugLine="qrscanner.theResultPointColor = Colors.Red";
Debug.ShouldStop(16);
mostCurrent._qrscanner.theResultPointColor = anywheresoftware.b4a.keywords.Common.Colors.Red;
 BA.debugLineNum = 231;BA.debugLine="qrscanner.theBottomPromptMessage = \"Scan Your Cof";
Debug.ShouldStop(64);
mostCurrent._qrscanner.theBottomPromptMessage = "Scan Your Coffee Stamp.";
 BA.debugLineNum = 232;BA.debugLine="qrscanner.theBottomPromptTextSize = 5%y";
Debug.ShouldStop(128);
mostCurrent._qrscanner.theBottomPromptTextSize = anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA);
 BA.debugLineNum = 233;BA.debugLine="qrscanner.bottomPromptColor = Colors.Yellow";
Debug.ShouldStop(256);
mostCurrent._qrscanner.bottomPromptColor = anywheresoftware.b4a.keywords.Common.Colors.Yellow;
 BA.debugLineNum = 234;BA.debugLine="qrscanner.bottomPromptDistanceFromBottom = 5%y";
Debug.ShouldStop(512);
mostCurrent._qrscanner.bottomPromptDistanceFromBottom = anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA);
 BA.debugLineNum = 236;BA.debugLine="scanSuccess = False";
Debug.ShouldStop(2048);
_scansuccess = anywheresoftware.b4a.keywords.Common.False;
 BA.debugLineNum = 237;BA.debugLine="qrscanner.BeginScan(\"scanner\")	'This is the funct";
Debug.ShouldStop(4096);
mostCurrent._qrscanner.BeginScan(mostCurrent.activityBA,"scanner");
 BA.debugLineNum = 240;BA.debugLine="End Sub";
Debug.ShouldStop(32768);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _btnyes_click() throws Exception{
try {
		Debug.PushSubsStack("btnyes_Click (main) ","main",0,mostCurrent.activityBA,mostCurrent,203);
 BA.debugLineNum = 203;BA.debugLine="Sub btnyes_Click 'to redeem your free coffee press";
Debug.ShouldStop(1024);
 BA.debugLineNum = 204;BA.debugLine="CoffeeCount = 0";
Debug.ShouldStop(2048);
_coffeecount = (int) (0);
 BA.debugLineNum = 205;BA.debugLine="updateStamps";
Debug.ShouldStop(4096);
_updatestamps();
 BA.debugLineNum = 206;BA.debugLine="Activity.LoadLayout(\"main\")";
Debug.ShouldStop(8192);
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 BA.debugLineNum = 208;BA.debugLine="End Sub";
Debug.ShouldStop(32768);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}

public static void initializeProcessGlobals() {
    if (mostCurrent != null && mostCurrent.activityBA != null) {
Debug.StartDebugging(mostCurrent.activityBA, 22909, new int[] {8, 2, 1, 1, 1, 3}, "8b5919a7-0d98-49fe-87d7-158f1067e604");}

    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
themecalc._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 31;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 36;BA.debugLine="Dim Card As Card";
mostCurrent._card = new b4a.example.card();
 //BA.debugLineNum = 37;BA.debugLine="Private btnScan As Button";
mostCurrent._btnscan = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Private imgLogo As ImageView";
mostCurrent._imglogo = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Private imgStamp1, imgStamp2, imgStamp3, imgStamp";
mostCurrent._imgstamp1 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgstamp2 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgstamp3 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgstamp4 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgstamp5 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgstamp6 = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Private lblCompanyName As Label";
mostCurrent._lblcompanyname = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Private pnlBG As Panel";
mostCurrent._pnlbg = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Private pnlStamp1, pnlStamp2, pnlStamp3, pnlStamp";
mostCurrent._pnlstamp1 = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._pnlstamp2 = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._pnlstamp3 = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._pnlstamp4 = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._pnlstamp5 = new anywheresoftware.b4a.objects.PanelWrapper();
mostCurrent._pnlstamp6 = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 44;BA.debugLine="Dim myTheme As CoffeeTheme";
mostCurrent._mytheme = new b4a.example.coffeetheme();
 //BA.debugLineNum = 45;BA.debugLine="Dim scanSuccess As Boolean";
_scansuccess = false;
 //BA.debugLineNum = 46;BA.debugLine="Dim qrscanner As JhsIceZxing1";
mostCurrent._qrscanner = new ice.zxing.b4aZXingLib();
 //BA.debugLineNum = 47;BA.debugLine="Private No As Button";
mostCurrent._no = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Private Yes As Button";
mostCurrent._yes = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 49;BA.debugLine="End Sub";
return "";
}
public static String  _loaddbbuttoncolours() throws Exception{
try {
		Debug.PushSubsStack("loadDBbuttonColours (main) ","main",0,mostCurrent.activityBA,mostCurrent,110);
int _i = 0;
anywheresoftware.b4a.objects.drawable.ColorDrawable _btnrbg = null;
int _colours = 0;
 BA.debugLineNum = 110;BA.debugLine="Sub loadDBbuttonColours ' method to assign colours";
Debug.ShouldStop(8192);
 BA.debugLineNum = 111;BA.debugLine="myButtonColours =myTheme.loadBtnColours";
Debug.ShouldStop(16384);
_mybuttoncolours = mostCurrent._mytheme._loadbtncolours();
 BA.debugLineNum = 112;BA.debugLine="For i = 0 To myButtonColours.RowCount - 1";
Debug.ShouldStop(32768);
{
final int step72 = 1;
final int limit72 = (int) (_mybuttoncolours.getRowCount()-1);
for (_i = (int) (0); (step72 > 0 && _i <= limit72) || (step72 < 0 && _i >= limit72); _i = ((int)(0 + _i + step72))) {
Debug.locals.put("i", _i);
 BA.debugLineNum = 113;BA.debugLine="myButtonColours.Position = i";
Debug.ShouldStop(65536);
_mybuttoncolours.setPosition(_i);
 BA.debugLineNum = 114;BA.debugLine="Dim btnRBG As ColorDrawable";
Debug.ShouldStop(131072);
_btnrbg = new anywheresoftware.b4a.objects.drawable.ColorDrawable();Debug.locals.put("btnRBG", _btnrbg);
 BA.debugLineNum = 115;BA.debugLine="Dim colours As Int";
Debug.ShouldStop(262144);
_colours = 0;Debug.locals.put("colours", _colours);
 BA.debugLineNum = 116;BA.debugLine="colours = Colors.RGB(myButtonColours.GetInt(\"Btn";
Debug.ShouldStop(524288);
_colours = anywheresoftware.b4a.keywords.Common.Colors.RGB(_mybuttoncolours.GetInt("BtnRed"),_mybuttoncolours.GetInt("BtnBlue"),_mybuttoncolours.GetInt("BtnGreen"));Debug.locals.put("colours", _colours);
 BA.debugLineNum = 117;BA.debugLine="btnRBG.Initialize(colours, 5)";
Debug.ShouldStop(1048576);
_btnrbg.Initialize(_colours,(int) (5));
 BA.debugLineNum = 118;BA.debugLine="btnScan.background=btnRBG";
Debug.ShouldStop(2097152);
mostCurrent._btnscan.setBackground((android.graphics.drawable.Drawable)(_btnrbg.getObject()));
 }
}Debug.locals.put("i", _i);
;
 BA.debugLineNum = 120;BA.debugLine="End Sub";
Debug.ShouldStop(8388608);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _loaddbcolours() throws Exception{
try {
		Debug.PushSubsStack("loadDBcolours (main) ","main",0,mostCurrent.activityBA,mostCurrent,69);
int _i = 0;
anywheresoftware.b4a.objects.drawable.GradientDrawable _bggradient = null;
int[] _colours = null;
 BA.debugLineNum = 69;BA.debugLine="Sub loadDBcolours 'Assign background colours from";
Debug.ShouldStop(16);
 BA.debugLineNum = 70;BA.debugLine="myColors = myTheme.loadColours";
Debug.ShouldStop(32);
_mycolors = mostCurrent._mytheme._loadcolours();
 BA.debugLineNum = 71;BA.debugLine="For i = 0 To myColors.RowCount - 1 '";
Debug.ShouldStop(64);
{
final int step35 = 1;
final int limit35 = (int) (_mycolors.getRowCount()-1);
for (_i = (int) (0); (step35 > 0 && _i <= limit35) || (step35 < 0 && _i >= limit35); _i = ((int)(0 + _i + step35))) {
Debug.locals.put("i", _i);
 BA.debugLineNum = 72;BA.debugLine="myColors.Position = i";
Debug.ShouldStop(128);
_mycolors.setPosition(_i);
 BA.debugLineNum = 73;BA.debugLine="Dim bgGradient As GradientDrawable";
Debug.ShouldStop(256);
_bggradient = new anywheresoftware.b4a.objects.drawable.GradientDrawable();Debug.locals.put("bgGradient", _bggradient);
 BA.debugLineNum = 74;BA.debugLine="Dim colours(2) As Int";
Debug.ShouldStop(512);
_colours = new int[(int) (2)];
;Debug.locals.put("colours", _colours);
 BA.debugLineNum = 75;BA.debugLine="colours(0) = Colors.RGB(myColors.GetInt(\"BG1Red\"";
Debug.ShouldStop(1024);
_colours[(int) (0)] = anywheresoftware.b4a.keywords.Common.Colors.RGB(_mycolors.GetInt("BG1Red"),_mycolors.GetInt("BG1Blue"),_mycolors.GetInt("BG1Green"));Debug.locals.put("colours", _colours);
 BA.debugLineNum = 76;BA.debugLine="colours(1) = Colors.RGB(myColors.GetInt(\"BG2Red\"";
Debug.ShouldStop(2048);
_colours[(int) (1)] = anywheresoftware.b4a.keywords.Common.Colors.RGB(_mycolors.GetInt("BG2Red"),_mycolors.GetInt("BG2Blue"),_mycolors.GetInt("BG2Green"));Debug.locals.put("colours", _colours);
 BA.debugLineNum = 77;BA.debugLine="bgGradient.Initialize(\"TR_BL\", colours)";
Debug.ShouldStop(4096);
_bggradient.Initialize(BA.getEnumFromString(android.graphics.drawable.GradientDrawable.Orientation.class,"TR_BL"),_colours);
 BA.debugLineNum = 78;BA.debugLine="pnlBG.Background=bgGradient";
Debug.ShouldStop(8192);
mostCurrent._pnlbg.setBackground((android.graphics.drawable.Drawable)(_bggradient.getObject()));
 }
}Debug.locals.put("i", _i);
;
 BA.debugLineNum = 80;BA.debugLine="End Sub";
Debug.ShouldStop(32768);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _loaddblogo() throws Exception{
try {
		Debug.PushSubsStack("loadDBlogo (main) ","main",0,mostCurrent.activityBA,mostCurrent,82);
int _i = 0;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _image = null;
 BA.debugLineNum = 82;BA.debugLine="Sub loadDBlogo 'Assign Logo as string from databas";
Debug.ShouldStop(131072);
 BA.debugLineNum = 83;BA.debugLine="myLogo=myTheme.loadLogo";
Debug.ShouldStop(262144);
_mylogo = mostCurrent._mytheme._loadlogo();
 BA.debugLineNum = 84;BA.debugLine="For i = 0 To myLogo.RowCount - 1";
Debug.ShouldStop(524288);
{
final int step47 = 1;
final int limit47 = (int) (_mylogo.getRowCount()-1);
for (_i = (int) (0); (step47 > 0 && _i <= limit47) || (step47 < 0 && _i >= limit47); _i = ((int)(0 + _i + step47))) {
Debug.locals.put("i", _i);
 BA.debugLineNum = 85;BA.debugLine="myLogo.Position=i";
Debug.ShouldStop(1048576);
_mylogo.setPosition(_i);
 BA.debugLineNum = 86;BA.debugLine="Dim image As Bitmap";
Debug.ShouldStop(2097152);
_image = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();Debug.locals.put("image", _image);
 BA.debugLineNum = 87;BA.debugLine="image.Initialize(File.DirAssets, myLogo.GetStrin";
Debug.ShouldStop(4194304);
_image.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),_mylogo.GetString("Logo"));
 BA.debugLineNum = 88;BA.debugLine="imgLogo.Bitmap=image";
Debug.ShouldStop(8388608);
mostCurrent._imglogo.setBitmap((android.graphics.Bitmap)(_image.getObject()));
 }
}Debug.locals.put("i", _i);
;
 BA.debugLineNum = 90;BA.debugLine="End Sub";
Debug.ShouldStop(33554432);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _loaddbname() throws Exception{
try {
		Debug.PushSubsStack("loadDBname (main) ","main",0,mostCurrent.activityBA,mostCurrent,92);
int _i = 0;
 BA.debugLineNum = 92;BA.debugLine="Sub loadDBname ' method to assign Company Name to";
Debug.ShouldStop(134217728);
 BA.debugLineNum = 93;BA.debugLine="myCoName=myTheme.loadCompanyName";
Debug.ShouldStop(268435456);
_myconame = mostCurrent._mytheme._loadcompanyname();
 BA.debugLineNum = 94;BA.debugLine="For i = 0 To myCoName.RowCount - 1";
Debug.ShouldStop(536870912);
{
final int step56 = 1;
final int limit56 = (int) (_myconame.getRowCount()-1);
for (_i = (int) (0); (step56 > 0 && _i <= limit56) || (step56 < 0 && _i >= limit56); _i = ((int)(0 + _i + step56))) {
Debug.locals.put("i", _i);
 BA.debugLineNum = 95;BA.debugLine="myCoName.Position=i";
Debug.ShouldStop(1073741824);
_myconame.setPosition(_i);
 BA.debugLineNum = 96;BA.debugLine="lblCompanyName.Text  =myCoName.GetString(\"Compan";
Debug.ShouldStop(-2147483648);
mostCurrent._lblcompanyname.setText((Object)(_myconame.GetString("CompanyName")));
 }
}Debug.locals.put("i", _i);
;
 BA.debugLineNum = 98;BA.debugLine="End Sub";
Debug.ShouldStop(2);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _loadstamp() throws Exception{
try {
		Debug.PushSubsStack("loadStamp (main) ","main",0,mostCurrent.activityBA,mostCurrent,100);
int _i = 0;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _image = null;
 BA.debugLineNum = 100;BA.debugLine="Sub loadStamp ' method to assign stamp image to St";
Debug.ShouldStop(8);
 BA.debugLineNum = 101;BA.debugLine="myStamp=myTheme.loadStampIcon";
Debug.ShouldStop(16);
_mystamp = mostCurrent._mytheme._loadstampicon();
 BA.debugLineNum = 102;BA.debugLine="For i = 0 To myStamp.RowCount - 1";
Debug.ShouldStop(32);
{
final int step63 = 1;
final int limit63 = (int) (_mystamp.getRowCount()-1);
for (_i = (int) (0); (step63 > 0 && _i <= limit63) || (step63 < 0 && _i >= limit63); _i = ((int)(0 + _i + step63))) {
Debug.locals.put("i", _i);
 BA.debugLineNum = 103;BA.debugLine="myStamp.Position=i";
Debug.ShouldStop(64);
_mystamp.setPosition(_i);
 BA.debugLineNum = 104;BA.debugLine="Dim image As Bitmap";
Debug.ShouldStop(128);
_image = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();Debug.locals.put("image", _image);
 BA.debugLineNum = 105;BA.debugLine="image.Initialize(File.DirAssets, myLogo.GetStrin";
Debug.ShouldStop(256);
_image.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),_mylogo.GetString("StampIcon"));
 BA.debugLineNum = 106;BA.debugLine="imgStamp1.Bitmap=image";
Debug.ShouldStop(512);
mostCurrent._imgstamp1.setBitmap((android.graphics.Bitmap)(_image.getObject()));
 }
}Debug.locals.put("i", _i);
;
 BA.debugLineNum = 108;BA.debugLine="End Sub";
Debug.ShouldStop(2048);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 18;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 21;BA.debugLine="Dim myColors As Cursor";
_mycolors = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Dim myLogo As Cursor";
_mylogo = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Dim myCoName As Cursor";
_myconame = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 24;BA.debugLine="Dim myStamp As Cursor";
_mystamp = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Dim Card As Card";
mostCurrent._card = new b4a.example.card();
 //BA.debugLineNum = 26;BA.debugLine="Dim CoffeeCount As Int";
_coffeecount = 0;
 //BA.debugLineNum = 27;BA.debugLine="Dim myButtonColours As Cursor";
_mybuttoncolours = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 29;BA.debugLine="End Sub";
return "";
}
public static String  _scanner_noscan(String _atype,String _values) throws Exception{
try {
		Debug.PushSubsStack("scanner_noscan (main) ","main",0,mostCurrent.activityBA,mostCurrent,251);
Debug.locals.put("atype", _atype);
Debug.locals.put("Values", _values);
 BA.debugLineNum = 251;BA.debugLine="Sub scanner_noscan(atype As String,Values As Strin";
Debug.ShouldStop(67108864);
 BA.debugLineNum = 252;BA.debugLine="Log(\"type:\" & atype &  \"Values:\" & Values)";
Debug.ShouldStop(134217728);
anywheresoftware.b4a.keywords.Common.Log("type:"+_atype+"Values:"+_values);
 BA.debugLineNum = 253;BA.debugLine="Msgbox(Values,\"Scan Failed\")";
Debug.ShouldStop(268435456);
anywheresoftware.b4a.keywords.Common.Msgbox(_values,"Scan Failed",mostCurrent.activityBA);
 BA.debugLineNum = 255;BA.debugLine="End Sub";
Debug.ShouldStop(1073741824);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _scanner_result(String _atype,String _values) throws Exception{
try {
		Debug.PushSubsStack("scanner_result (main) ","main",0,mostCurrent.activityBA,mostCurrent,242);
Debug.locals.put("atype", _atype);
Debug.locals.put("Values", _values);
 BA.debugLineNum = 242;BA.debugLine="Sub scanner_result(atype As String,Values As Strin";
Debug.ShouldStop(131072);
 BA.debugLineNum = 243;BA.debugLine="If Values <> \"Null\" Then";
Debug.ShouldStop(262144);
if ((_values).equals("Null") == false) { 
 BA.debugLineNum = 244;BA.debugLine="scanSuccess = True";
Debug.ShouldStop(524288);
_scansuccess = anywheresoftware.b4a.keywords.Common.True;
 BA.debugLineNum = 245;BA.debugLine="CoffeeCount = CoffeeCount + 1";
Debug.ShouldStop(1048576);
_coffeecount = (int) (_coffeecount+1);
 BA.debugLineNum = 246;BA.debugLine="Log(\"type:\" & atype &  \"Values:\" & Values)";
Debug.ShouldStop(2097152);
anywheresoftware.b4a.keywords.Common.Log("type:"+_atype+"Values:"+_values);
 };
 BA.debugLineNum = 248;BA.debugLine="updateStamps";
Debug.ShouldStop(8388608);
_updatestamps();
 BA.debugLineNum = 249;BA.debugLine="End Sub";
Debug.ShouldStop(16777216);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
public static String  _updatestamps() throws Exception{
try {
		Debug.PushSubsStack("updateStamps (main) ","main",0,mostCurrent.activityBA,mostCurrent,131);
 BA.debugLineNum = 131;BA.debugLine="Private Sub updateStamps";
Debug.ShouldStop(4);
 BA.debugLineNum = 132;BA.debugLine="If CoffeeCount = 6 Then";
Debug.ShouldStop(8);
if (_coffeecount==6) { 
 BA.debugLineNum = 133;BA.debugLine="ToastMessageShow(\" You Have Earned A free Coffee\"";
Debug.ShouldStop(16);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" You Have Earned A free Coffee",anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 134;BA.debugLine="Activity.LoadLayout(\"freecoffee\") 'will load 2 la";
Debug.ShouldStop(32);
mostCurrent._activity.LoadLayout("freecoffee",mostCurrent.activityBA);
 BA.debugLineNum = 135;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(64);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 136;BA.debugLine="imgStamp2.Visible = True";
Debug.ShouldStop(128);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 137;BA.debugLine="imgStamp3.Visible = True";
Debug.ShouldStop(256);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 138;BA.debugLine="imgStamp4.Visible = True";
Debug.ShouldStop(512);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 139;BA.debugLine="imgStamp5.Visible = True";
Debug.ShouldStop(1024);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 140;BA.debugLine="imgStamp6.Visible = True";
Debug.ShouldStop(2048);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.True);
 };
 BA.debugLineNum = 143;BA.debugLine="If CoffeeCount = 5 Then 'from 5 to 1 will show ho";
Debug.ShouldStop(16384);
if (_coffeecount==5) { 
 BA.debugLineNum = 144;BA.debugLine="ToastMessageShow (\" You still need 1 more stamp i";
Debug.ShouldStop(32768);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" You still need 1 more stamp in order to get a free coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 145;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(65536);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 146;BA.debugLine="imgStamp2.Visible = True";
Debug.ShouldStop(131072);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 147;BA.debugLine="imgStamp3.Visible = True";
Debug.ShouldStop(262144);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 148;BA.debugLine="imgStamp4.Visible = True";
Debug.ShouldStop(524288);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 149;BA.debugLine="imgStamp5.Visible = True";
Debug.ShouldStop(1048576);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 150;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(2097152);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 153;BA.debugLine="If CoffeeCount = 4 Then";
Debug.ShouldStop(16777216);
if (_coffeecount==4) { 
 BA.debugLineNum = 154;BA.debugLine="ToastMessageShow (\" You still need 2 more stamp i";
Debug.ShouldStop(33554432);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" You still need 2 more stamp in order to get a free coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 155;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(67108864);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 156;BA.debugLine="imgStamp2.Visible = True";
Debug.ShouldStop(134217728);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 157;BA.debugLine="imgStamp3.Visible = True";
Debug.ShouldStop(268435456);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 158;BA.debugLine="imgStamp4.Visible = True";
Debug.ShouldStop(536870912);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 159;BA.debugLine="imgStamp5.Visible = False";
Debug.ShouldStop(1073741824);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 160;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(-2147483648);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 162;BA.debugLine="If CoffeeCount = 3 Then";
Debug.ShouldStop(2);
if (_coffeecount==3) { 
 BA.debugLineNum = 163;BA.debugLine="ToastMessageShow (\" You still need 3 more stamp i";
Debug.ShouldStop(4);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" You still need 3 more stamp in order to get a free coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 164;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(8);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 165;BA.debugLine="imgStamp2.Visible = True";
Debug.ShouldStop(16);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 166;BA.debugLine="imgStamp3.Visible = True";
Debug.ShouldStop(32);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 167;BA.debugLine="imgStamp4.Visible = False";
Debug.ShouldStop(64);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 168;BA.debugLine="imgStamp5.Visible = False";
Debug.ShouldStop(128);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 169;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(256);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 171;BA.debugLine="If CoffeeCount = 2 Then";
Debug.ShouldStop(1024);
if (_coffeecount==2) { 
 BA.debugLineNum = 172;BA.debugLine="ToastMessageShow (\" you still need 4  more stamp";
Debug.ShouldStop(2048);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" you still need 4  more stamp in order to get a free coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 173;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(4096);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 174;BA.debugLine="imgStamp2.Visible = True";
Debug.ShouldStop(8192);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 175;BA.debugLine="imgStamp3.Visible = False";
Debug.ShouldStop(16384);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 176;BA.debugLine="imgStamp4.Visible = False";
Debug.ShouldStop(32768);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 177;BA.debugLine="imgStamp5.Visible = False";
Debug.ShouldStop(65536);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 178;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(131072);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 180;BA.debugLine="If CoffeeCount = 1 Then";
Debug.ShouldStop(524288);
if (_coffeecount==1) { 
 BA.debugLineNum = 181;BA.debugLine="ToastMessageShow (\" You still need 5  more stamp";
Debug.ShouldStop(1048576);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" You still need 5  more stamp in order to get a free coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 182;BA.debugLine="imgStamp1.Visible = True";
Debug.ShouldStop(2097152);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.True);
 BA.debugLineNum = 183;BA.debugLine="imgStamp2.Visible = False";
Debug.ShouldStop(4194304);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 184;BA.debugLine="imgStamp3.Visible = False";
Debug.ShouldStop(8388608);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 185;BA.debugLine="imgStamp4.Visible = False";
Debug.ShouldStop(16777216);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 186;BA.debugLine="imgStamp5.Visible = False";
Debug.ShouldStop(33554432);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 187;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(67108864);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 190;BA.debugLine="If CoffeeCount = 0 Then";
Debug.ShouldStop(536870912);
if (_coffeecount==0) { 
 BA.debugLineNum = 191;BA.debugLine="ToastMessageShow (\" Don't Forget to use your Coff";
Debug.ShouldStop(1073741824);
anywheresoftware.b4a.keywords.Common.ToastMessageShow(" Don't Forget to use your Coffe-E-Card when you buying  a coffee",anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 192;BA.debugLine="imgStamp1.Visible = False";
Debug.ShouldStop(-2147483648);
mostCurrent._imgstamp1.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 193;BA.debugLine="imgStamp2.Visible = False";
Debug.ShouldStop(1);
mostCurrent._imgstamp2.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 194;BA.debugLine="imgStamp3.Visible = False";
Debug.ShouldStop(2);
mostCurrent._imgstamp3.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 195;BA.debugLine="imgStamp4.Visible = False";
Debug.ShouldStop(4);
mostCurrent._imgstamp4.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 196;BA.debugLine="imgStamp5.Visible = False";
Debug.ShouldStop(8);
mostCurrent._imgstamp5.setVisible(anywheresoftware.b4a.keywords.Common.False);
 BA.debugLineNum = 197;BA.debugLine="imgStamp6.Visible = False";
Debug.ShouldStop(16);
mostCurrent._imgstamp6.setVisible(anywheresoftware.b4a.keywords.Common.False);
 };
 BA.debugLineNum = 199;BA.debugLine="End Sub";
Debug.ShouldStop(64);
return "";
}
catch (Exception e) {
			Debug.ErrorCaught(e);
			throw e;
		} 
finally {
			Debug.PopSubsStack();
		}}
}
