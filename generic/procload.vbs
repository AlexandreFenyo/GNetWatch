' cscript procload.vbs CPU0 host user@domain pass //Nologo

set objArgs = WScript.Arguments

Set objLocator = CreateObject("WbemScripting.SWbemLocator")
Set objWMIService = objLocator.ConnectServer(objArgs(1), "root\cimv2", objArgs(2), objArgs(3))
objWMIService.Security_.ImpersonationLevel = 3

wqlQuery = "select LoadPercentage from Win32_Processor where DeviceID = '" & objArgs(0) & "'"

for each oData in objWMIService.ExecQuery(wqlQuery)
  for each oProperty in oData.Properties_
    if oProperty.Name = "LoadPercentage" then
      proc0Load = oProperty.Value
    end if
  next
next

wscript.echo proc0Load
