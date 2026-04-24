# Lab6 Score Sync Server

This is a plain Java relay server for online battle score synchronization.

## Run

```powershell
cd E:\Android\AircraftWar-Android\lab6-server
javac ScoreSyncServer.java
java ScoreSyncServer 9999
```

## Notes

- Android emulator can connect with host `10.0.2.2`.
- Physical phone should use your PC LAN IP.
- Every message is relayed to all other connected clients.
