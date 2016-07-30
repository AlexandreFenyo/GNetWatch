' cscript freephysmem.vbs host user@domain pass //Nologo

set objArgs = WScript.Arguments

Set objLocator = CreateObject("WbemScripting.SWbemLocator")
Set objWMIService = objLocator.ConnectServer(objArgs(0), "root\cimv2", objArgs(1), objArgs(2))
objWMIService.Security_.ImpersonationLevel = 3

' FreePhysicalMemory, FreeVirtualMemory, TotalVirtualMemorySize
wqlQuery = "SELECT FreePhysicalMemory FROM Win32_OperatingSystem"

for each oData in objWMIService.ExecQuery(wqlQuery)
  for each oProperty in oData.Properties_
    if oProperty.Name = "FreePhysicalMemory" then
      intvalue = oProperty.Value
    end if
  next
next

wscript.echo intvalue
