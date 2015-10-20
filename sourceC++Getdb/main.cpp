#include <iostream>
#include <cstdio>
#include <conio.h>
#include <string>
#include <string.h>
#include <vector>
#include <Windows.h>
using namespace std;

string exec(char* cmd) {
    cout<<cmd<<endl;
    FILE* pipe = popen(cmd, "r");
    if (!pipe) return "ERROR";
    char buffer[128];
    std::string result = "";
    while(!feof(pipe)) {
    	if(fgets(buffer, 128, pipe) != NULL)
    		result += buffer;
    }
    pclose(pipe);
    return result;
}

//test.exe "path to main db"

int main(int argc, char* argv[])
{
    system("adb devices");
    if(argc==3 || argc==1){
        string path=argv[1];
        string package=argv[2];
        if(package=="") package="com.lcl.sunnypoints";
        string command="adb shell su -c cp /data/data/"+package+"/databases/main.db sdcard/main.db";
        string command2="adb pull sdcard/main.db "+ path +"main.db";
        char* cmd= new char[command.length()+1];
        strcpy(cmd, command.c_str());
        char* cmd2= new char[command2.length()+1];
        strcpy(cmd2, command2.c_str());
        exec(cmd);
        exec(cmd2);

        command2=path+"sqlitebrowser/sqliteman.exe "+path+"main.db";
        cmd2= new char[command2.length()+1];
        strcpy(cmd2, command2.c_str());
        exec(cmd2);

        system("pause");
    }else if(argc==2){
        string path=argv[1];
        string command2="adb pull sdcard/main.db "+ path +"main.db";
        char* cmd2= new char[command2.length()+1];
        strcpy(cmd2, command2.c_str());
        exec(cmd2);

        command2=path+"sqlitebrowser/sqliteman.exe "+path+"main.db";
        cmd2= new char[command2.length()+1];
        strcpy(cmd2, command2.c_str());
        exec(cmd2);

        system("pause");

    }

    return 0;
}
