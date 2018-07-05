pragma solidity ^0.4.18;

import "./string.sol";

contract FiveW {

using strings for *;

uint DECIMAL = 10000;

enum State { NewlyCreated, ConsensusFiveW, ConsensusTrustiness}

string[] public test; //just for DEBUG
uint public testInt; //just for DEBUG

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

FiveWSentence[]  questionsHigh;
FiveWSentence[]  questionsLow;
// prepare predefined list of 5w where to add according to trustiness, in order to speed up, but consuming memory
// future: integrate with second layer -> another list

struct Metainfo {
string name;
string  hash;
bytes32 hash5w;
FiveWSentence[] fiveWMap;
string[] claim;
uint trustiness; //suppose trustiness in % 100,00 so 10^3
State state;
}

mapping (uint => Metainfo) public news;
uint newsLen = 0;
mapping (string => uint) newsID; //the key hash of metainfo is used for update purpose
string[] hash5wString;

mapping (string => uint[]) votesRes; //key is resource hash, the other is [ACK++, NACK++]
mapping (string => address[]) voters; //key is resource hash, the other is people that have voted

mapping (string => byte[]) payloads; //struct for saving resource whose 5w consensus has to be achieved

/*
NOW STARTS THE FIRST CALL, WE JUST SAVE METAINFO AS NEWLY CREATED AND PAYLOAD
*/
function startFiveW(string name,
string  hash,
byte[] payloadRes, //USED AS INPUT FOR EXTRACTING FIVEW
string claims, //WITH "-" AS DELIMITER FOR STRINGS
string meta5w,//SEPARATED BY CUSTOM DELIMITER
uint[] accuracies
) public {

newsID[hash] = newsLen; //increment counter
newsLen++;

// INSTEAD OF DECLARING VARIABLE AND THEN ADD, JUST UPDATE VALUE IN MAPPING!!!
news[newsID[hash]].name = name;
news[newsID[hash]].hash = hash; //MAYBE COMPUTE - FIXME
news[newsID[hash]].claim = split(claims,"-");
news[newsID[hash]].state = State.NewlyCreated;

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
news[newsID[hash]].hash5w = sha256(abi.encodePacked(list)); //list used as in memory variable for hash
hash5wString.push(meta5w); //PUSH FOR CHECK
payloads[hash] = payloadRes;

}
//FOR TEST:
/*
before populate with start5w :
"prova", "reshash",["0x2345"],"claim1-claim2","a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#",[1,1,1,1,1,1,1,1,1,1]
now invoke add5w:
"reshash","a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#",[1,1,1,1,1,1,1,1,1,1]
*/
function add5w(string hash, string extracted, uint[] extAccuracy) public {
bool found = false;
string[] memory parts = split5w(extracted, extAccuracy);
FiveWSentence[] memory list = new FiveWSentence[](5); //for now declerad fixed maximum size 5
FiveWSentence memory fivew;
uint i; //for loops
for(i = 0; i < extAccuracy.length/5; i++){
if (i > 5) break; //MAX SIZE BY DEFAULT
fivew.whereName = parts[5*i];
fivew.whenName = parts[5*i+1];
fivew.whoName = parts[5*i+2];
fivew.dativeName = parts[5*i+3];
fivew.whatName = parts[5*i+4];
fivew.whereAccuracy = extAccuracy[5*i];
fivew.whenAccuracy = extAccuracy[5*i+1];
fivew.whoAccuracy = extAccuracy[5*i+2];
fivew.dativeAccuracy = extAccuracy[5*i+3];
fivew.whatAccuracy = extAccuracy[5*i+4];
list[i] = fivew;
}

//meta get by hash directly in if STATEMENT

if (votesRes[hash].length < 2) {
votesRes[hash].push(0);
votesRes[hash].push(0);
}
if (sha256(abi.encodePacked(list)) == news[newsID[hash]].hash5w) { //check if same 5w already extracted FIXME
test[0] = '1';
voters[hash].push(msg.sender);
votesRes[hash][0] = votesRes[hash][0] +1;
} else {
test[0] = '2';
voters[hash].push(msg.sender);
votesRes[hash][1] = votesRes[hash][1] +1;
}

if ( voters[hash].length > 0){ //DEFINE A TRESHOLD AND TRIGGER TRUSTINESS AND SO ON BASED ON #VOTERS
//consensus achieved
if (votesRes[hash][1] < votesRes[hash][0]) {
test[0] = 'CONSENSUS NACK';
//delete news[newsID[hash]]; //OUT OF GAS FIXME
} else {
//compute trustiness
//ALGORITHM
uint trustValue = 1;
//for all string (5w) extracted computeTrustiness, then average, but now do algorithm just one time to test
//for testing just one
for (i = 0; i < list.length; i++) { //OUT OF GAS FIXME algorithm
trustValue += computeTrustiness(list[i].whoName,list[i].whereName,list[i].whenName, list[i].whatName, list[i].dativeName);
//at the end compute evarage by dividing for lenght
}
if (trustValue > i) {
trustValue /= i;
} else {
trustValue = 0;
}

//Metainfo meta = news[newsID[hash]]; CURRENT VARIABLE TO BE UPDATED
news[newsID[hash]].state = State.ConsensusFiveW;
news[newsID[hash]].trustiness = trustValue;
if (trustValue > 6000) { //EXAMPLE 60%
news[newsID[hash]].state = State.ConsensusTrustiness;
for (i = 0; i < list.length; i++) {
questionsHigh.push(list[i]);
}
//VARIABLE UPDATED OK
} else if (trustValue > 2000) { //EXAMPLE 20%
news[newsID[hash]].state = State.ConsensusTrustiness;
for (i = 0; i < list.length; i++) {
questionsLow.push(list[i]);
}
//VARIABLE UPDATED OK
} else {
//delete news[newsID[hash]]; //OUT OF GAS FIXME
}
//consensus whole meta NO NEED
//now let's clear

//delete payloads[hash]; //OUT OF GAS FIXME
//delete votesRes[hash]; //OUT OF GAS FIXME
//delete voters[hash]; //OUT OF GAS FIXME

}

//now everything is complete, we want just to add the resource do cassandra FIXME
}
}

/*
* FUNCTION ALGORITHM TRUSTINESS
*
*
*/
function computeTrustiness(string who, string where, string when, string what, string dative) internal view returns(uint) { //other
uint T = 8000; //treshold for trustiness uint N = T/10; //max num of document to be analyzed
uint critical = 2000; //critical value (minimum for trusted news)
FiveWSentence[] questions;
while (T > critical) { //MANAGE TRUSTINESS SETS
if( T > 7000) {
questions = questionsHigh;
} else {
questions = questionsLow;
}
uint accuracy = 8000; //defined as trustiness of document
while (accuracy > 3000) { //under 30% no sense
//compute P(A|Where&When)*P(B|Where&When)*P(Action|Where&When)*P(Action|A)*P(Action|B)
//LET US DEFINE AN ARRAY OF UINT OTHERWISE STACK TOO DEEP (MAX 16)
//error DIVISION BY ZERO
uint[] memory probCount = new uint[](8);
probCount[0] = 0; //countContext
probCount[1] = 0; //countActorinContext
probCount[2] = 0; //countDativeinContext
probCount[3] = 0; //countActioninContext
probCount[4] = 0; //countActor
probCount[5] = 0; //countDative
probCount[6] = 0; //countActioninActor
probCount[7] = 0; //countActioninDative
for (uint i = 0; i < questions.length; i++){ //to questions.length
if (compareStrings(questions[i].whereName,where) &&
compareStrings(questions[i].whenName,when) &&
questions[i].whereAccuracy >= accuracy &&
questions[i].whenAccuracy >= accuracy) {
probCount[0] += 1;
if (compareStrings(questions[i].whoName,who)) {
probCount[1] = probCount[1] + questions[i].whoAccuracy;
}
if (compareStrings(questions[i].dativeName,dative)) {
probCount[2] = probCount[2] + questions[i].dativeAccuracy;
}
if (compareStrings(questions[i].whatName,what)) {
probCount[3] = probCount[3] + questions[i].whatAccuracy;
}
}
if (compareStrings(questions[i].whoName,who) &&
questions[i].whoAccuracy >= accuracy) {
probCount[4] += 1;
if (compareStrings(questions[i].whatName,what)) {
probCount[6] = probCount[6] + questions[i].whatAccuracy;
}
}
if (compareStrings(questions[i].dativeName,dative) &&
questions[i].dativeAccuracy >= accuracy) {
probCount[5] += 1;
if (compareStrings(questions[i].whatName,what)) {
probCount[7] = probCount[7] + questions[i].whatAccuracy;
}
}
}
if (probCount[0] + probCount[4] + probCount[5] > T/100) { //FIXME TRESHOLD VALUE INSTEAD OF 0 (T/100 tune)
//you can end and calculate value (THE FIRST PART INVOLVES ACCURACY)
//divide by decimal^num if multiplication
if(probCount[0] == 0) return uint(100);
if(probCount[4] == 0) return uint(104);
if(probCount[5] == 0) return uint(105);
//MAYBE MANAGE SWITCH CASE INSTEAD OF RETURNING ERROR
return (accuracy)*(probCount[1]/probCount[0])*(probCount[2]/probCount[0])*
(probCount[3]/probCount[0])*(probCount[6]/probCount[4])
*(probCount[7]/probCount[5])/(DECIMAL**5);
}
accuracy -= 3000;
}
T -= 7000; //RESET, AFTER MANAGING TRY TO ADAPT
}
return uint(1); //PROBLEM WITH 0 !!!!!!!!!!!!!!!!!!!!!
}

function getPayload(string hash) public view returns (byte[]) {
return payloads[hash];
}

function isHash5wPresent(string meta) public constant returns (bool) {
for (uint i = 0; i < hash5wString.length; i++) {
if (compareStrings(meta, hash5wString[i])) {
return true;
}
}
return false;
}

function populateTestFiveW() public {
FiveWSentence memory fivew;
fivew.whereName = "a";
fivew.whenName = "a";
fivew.whoName = "a";
fivew.dativeName = "a";
fivew.whatName = "a";
fivew.whenAccuracy = 8000;
fivew.whereAccuracy = 8000;
fivew.whoAccuracy = 8000;
fivew.dativeAccuracy = 8000;
fivew.whatAccuracy = 8000;
questionsHigh.push(fivew);
questionsLow.push(fivew);
}

function compareStrings (string a, string b) internal pure returns (bool){
return keccak256(abi.encodePacked(a)) == keccak256(abi.encodePacked(b));
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

