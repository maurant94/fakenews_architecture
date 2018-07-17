
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

struct Metainfo {
string name;
string  hash;
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

function whitelistAddress (address user) onlyOwner public {
userAddr[user] = true;
}

//READ FROM 1L, UPDATE LIST PUBLIC
function addNewsToCheck (string name,
string  hash,
string claims, //WITH "-" AS DELIMITER FOR STRINGS
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

}
