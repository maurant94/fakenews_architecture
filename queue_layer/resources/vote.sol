
pragma solidity ^0.4.18;

import "./string.sol";

contract Vote {
address owner;

function Vote() {
owner = msg.sender;
}

modifier onlyOwner() {
require(msg.sender == owner);
_;
}
}

contract Whitelist is Vote {

using strings for *;

uint DECIMAL = 10000;

enum State { NewlyCreated, Checked}

event VoteEvent(string ID, bool returnValue); //for log, so call-back clinet-side

string[] test; //just for DEBUG so public
uint testInt; //just for DEBUG so public

struct FiveWSentence {
string whoName;
uint whoAccuracy;
string dativeName;
uint dativeAccuracy;
string whatName;
uint whatAccuracy;
string whereName;
uint whereAccuracy;
string whenName;
uint whenAccuracy;
//for now why not used
//string whyName;
//uint whyAccuracy;
}

struct Metainfo {
string name;
string  hash;
bytes32 hash5w;
FiveWSentence[] fiveWMap;
string[] claim;
uint trustiness; //suppose trustiness in % 100,00 so 10^3
State state;
}

uint public newsLen = 0;
mapping (string => uint) newsID; //the key hash of metainfo is used for update purpose
mapping (uint => Metainfo) public news;

struct Voters {
uint ackVotes;
uint nackVotes;
uint quorum;
address[] addresses;
uint adrsLen;
bool exist;
}
mapping(string => Voters) checkScore;

mapping (address => bool) userAddr;

modifier onlyWhiteListed() {
require(userAddr[msg.sender] == true);
_;
}

function whitelistAddress (address user) onlyOwner {
userAddr[user] = true;
}

//READ FROM 1L, UPDATE LIST PUBLIC
function addNewsToCheck (string name,
string  hash,
string claims, //WITH "-" AS DELIMITER FOR STRINGS
string meta5w,//SEPARATED BY CUSTOM DELIMITER
uint[] accuracies,
bytes32 hash5w,
uint trustiness) onlyWhiteListed {

if (checkScore[hash].exist == true) {
return; //ALREADY SENT
}

//prepare received data
newsID[hash] = newsLen; //increment counter
newsLen++;

// INSTEAD OF DECLARING VARIABLE AND THEN ADD, JUST UPDATE VALUE IN MAPPING!!!
news[newsID[hash]].name = name;
news[newsID[hash]].hash = hash; //MAYBE COMPUTE - FIXME
news[newsID[hash]].claim = split(claims,"-");
news[newsID[hash]].state = State.NewlyCreated;
news[newsID[hash]].trustiness = trustiness;

//5w
string[] memory parts = split5w(meta5w, accuracies);
FiveWSentence memory fivew;
FiveWSentence[] memory list = new FiveWSentence[](5); //for now declerad fixed maximum size 5
for(uint i = 0; i < accuracies.length/5; i++){
if (i > 5) break; //MAX SIZE BY DEFAULT
fivew.whereName = parts[5*i];
fivew.whenName = parts[5*i+1];
fivew.whoName = parts[5*i+2];
fivew.dativeName = parts[5*i+3];
fivew.whatName = parts[5*i+4];
fivew.whereAccuracy = accuracies[5*i];
fivew.whenAccuracy = accuracies[5*i+1];
fivew.whoAccuracy = accuracies[5*i+2];
fivew.dativeAccuracy = accuracies[5*i+3];
fivew.whatAccuracy = accuracies[5*i+4];
news[newsID[hash]].fiveWMap.push(fivew);
list[i] = fivew;
}
news[newsID[hash]].hash5w = hash5w;

//PREPARE STRUCT FOR NEWS TO BE CHECKED
checkScore[hash].quorum = trustiness / 13; //TUNE PARAMETER
checkScore[hash].ackVotes = 0;
checkScore[hash].nackVotes = 0;
checkScore[hash].adrsLen = 0;
checkScore[hash].exist = true;

}

//COLLECT VOTE AND QUORUM, vote can be 0, 1 [nack, ack]
function checkTempNews (uint vote, string  hash) onlyWhiteListed {
bool found = false;
//CHECK IF ALREADY VOTED
for (uint i = 0; i < checkScore[hash].adrsLen; i++) {
if (msg.sender == checkScore[hash].addresses[i]) {
found = true;
}
}
//add vote
if (!found) {
checkScore[hash].addresses.push(msg.sender);
checkScore[hash].adrsLen++;
if (vote == 1) { //IT IS AN ACK
checkScore[hash].ackVotes++;
}
else {
checkScore[hash].nackVotes++;
}
}
//CHECK QUORUM REACHED
if (checkScore[hash].adrsLen > checkScore[hash].quorum) {
//NOW MOVE LIST IN CHECKED, OR UPDATE STATUS FIXME
news[newsID[hash]].state = State.Checked;
}
}

function split(string claims, string separator) internal pure returns (string[]) {
var s = claims.toSlice();
var delim = separator.toSlice();
var parts = new string[](s.count(delim));
for(uint i = 0; i < parts.length; i++) {
parts[i] = s.split(delim).toString();
}
return parts;
}

function split5wUtil(string sentences) internal pure returns (string[]) {
//multiple sentence delimier #+#, for each part (non empty) #-#
//example TEST "a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#",[1,1,1,1,1,1,1,1,1,1]
string[] memory s = split(sentences,"#-#");
string[] memory ret = new string[](10);
for (uint i = 0; i < s.length; i++){
string[] memory tmp = split(s[i],"#+#");
for (uint j = 0; j <5; j++) {
ret[5*i+j] = tmp[j];
}
}
return ret;
}

function split5w(string sentences, uint[] accuracies) internal returns (string[]) {
FiveWSentence[] storage fw;
FiveWSentence memory fivew;
test = split5wUtil(sentences);
string[] parts = test;

return parts;
}
}
