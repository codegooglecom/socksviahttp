unit MainUnit;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, JavaRuntime, JNI, JNIWrapper, StdCtrls, Menus,
  LMDCustomComponent, LMDWndProcComponent, LMDTrayIcon, iniFiles,
  LMDStarter;

const
  SVH_DEFAULT           = 'client';
  SVH_SECTION           = 'socksViaHttp';
  SVH_INIFILE           = 'SVHLaunch.ini';
  SVH_MAINCLASS         = 'MainClass';
  SVH_MAINARGS          = 'MainArgs';
  SVH_ICON              = 'Icon';
  SVH_LABEL             = 'Label';
  SVH_CONFIGFILE        = 'Configfile';
  SVH_CONFIGFILELOGITEM = 'ConfigFileLogItem';

type
  TMainForm = class(TForm)
    TrayIcon: TLMDTrayIcon;
    TrayPopup: TPopupMenu;
    TrayRestart: TMenuItem;
    TrayExit: TMenuItem;
    TrayEditLogs: TMenuItem;
    TrayEditConfiguration: TMenuItem;
    TrayEditSeparator: TMenuItem;
    TrayAbout: TMenuItem;
    LMDStarter: TLMDStarter;
    TrayAboutSeparator: TMenuItem;
    procedure TrayExitClick(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure TrayEditLogsClick(Sender: TObject);
    procedure TrayEditConfigurationClick(Sender: TObject);
    procedure TrayRestartClick(Sender: TObject);
    procedure TrayAboutClick(Sender: TObject);
    procedure TrayIconDblClick(Sender: TObject);
  private    { Private declarations }
    MainClass:String;
    MainArgs:String;
    Icon:String;
    AppLabel:String;
    Configfile:String;
    ConfigFileLogItem:String;
    LogFile:String;
    IniFileName:String;
    RunTime:TJavaRuntime;
    procedure ReadIni;
  end;

var
  MainForm: TMainForm;

implementation uses AboutUnit;

{$R *.dfm}
procedure TMainForm.TrayRestartClick(Sender: TObject);
var args:TStringList;
begin
  Try
    if MainArgs<>'' then begin
      args:=TStringList.Create;
      args.Add(MainArgs);
    end else args:=nil;
    RunTime.CallMain(MainClass,args);
  except
    on e:Exception do showMessage('Error [TrayRestartClick]: '+e.Message);
  end;
end;

procedure TMainForm.TrayExitClick(Sender: TObject);
begin
  Application.Terminate;
end;

procedure TMainForm.FormCreate(Sender: TObject);
begin
  if paramcount=0 then begin
    showMessage('Please do not run this application directly.'+#13+
                'Use batch files located in:'+#13+ExtractFilePath(paramStr(0)));
    Application.Terminate;
  end else begin
    RunTime:=TJavaRuntime.GetDefault;
    ReadIni;
    TrayRestartClick(self);
  end;
end;

procedure TMainForm.ReadIni;
var
  iniFile:TIniFile;
  section:String;
  path:String;

begin
  try
    path:=ExtractFilePath(paramStr(0));
    if path[length(path)]<>'\' then path:=path+'\';
    SetCurrentDir(path);
    if paramCount>=2 then begin
      IniFileName:=path+paramstr(2);
    end else begin
      IniFileName:=path+SVH_INIFILE;
    end;
    // Reading Ini
    if ParamCount>=1 then section:=ParamStr(1)
                     else section:=SVH_DEFAULT;
    if not FileExists(iniFileName) then raise Exception.Create(IniFileName+' not found');
    iniFile:=TIniFile.Create(IniFileName);
    if not iniFile.SectionExists(section) then raise Exception.Create('Section "'+section+'" not found in '+IniFileName);
    MainClass:=iniFile.ReadString(section,SVH_MAINCLASS,'');
    MainArgs:=iniFile.ReadString(section,SVH_MAINARGS,'');
    Icon:=path+iniFile.ReadString(section,SVH_ICON,'');
    AppLabel:=iniFile.ReadString(section,SVH_LABEL,'');
    Configfile:=iniFile.ReadString(section,SVH_CONFIGFILE,'');
    ConfigFileLogItem:=iniFile.ReadString(section,SVH_CONFIGFILELOGITEM,'');
    FreeAndNil(iniFile);
    // Initializing
    TrayIcon.Hint:=AppLabel;
    TrayIcon.Icon.LoadFromFile(Icon);
    // Reading Ini
    iniFile:=TIniFile.Create(Configfile);
    LogFile:=Path+iniFile.ReadString(SVH_SECTION,ConfigFileLogItem,'');
    FreeAndNil(iniFile);
  except
    on e:Exception do begin
      showMessage('Error [ReadIni]: '+e.Message);
      Application.Terminate;
    end;
  end;
end;

procedure TMainForm.TrayEditLogsClick(Sender: TObject);
begin
  LMDStarter.Parameters:=LogFile;
  LMDStarter.Execute;
end;

procedure TMainForm.TrayEditConfigurationClick(Sender: TObject);
begin
  if MessageDlg('If you modify this file, please restart this application',mtInformation,mbOKCancel,0)=mrOk then begin
    LMDStarter.Parameters:=Configfile;
    LMDStarter.Execute;
  end;
end;

procedure TMainForm.TrayAboutClick(Sender: TObject);
begin
  AboutForm.Caption:='About - '+AppLabel;
  AboutForm.Show;
end;

procedure TMainForm.TrayIconDblClick(Sender: TObject);
begin
  TrayAboutClick(self);
end;

end.
