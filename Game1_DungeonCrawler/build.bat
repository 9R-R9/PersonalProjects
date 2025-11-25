::@echo off
javac Game.java View.java Controller.java Model.java Tree.java Link.java Json.java Sprite.java TreasureChests.java Boomerang.java
if %errorlevel% neq 0 (
	echo There was an error; exiting now.	
) else (
	echo Compiled correctly!  Running Game...
	java Game	
)

