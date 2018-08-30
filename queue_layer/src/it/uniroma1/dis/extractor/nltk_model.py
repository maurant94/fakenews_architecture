from nltk.corpus import propbank
import nltk
from nltk.corpus import wordnet as wn
import re

class Features:
    pass

def readFile(file_path="./CoNLL2009-ST-English-train.txt"):
    sentences = []
    sentence = []
    verbsWithLabel = []
    verbsNoLabel = []
    for line in open(file_path, 'r'):
        line = line.strip().lower()
        words = re.split(r'\t+', line)
        if (words[0] != None and words[0] != ''):  # with space as words[0] starts a new sentence
            sentence.append(words[1])
            if (words[12] == 'y'):
                verbsNoLabel.append(words[1])
                verbsWithLabel.append(words[13])
        else:
            obj = Features()
            obj.sentence = sentence
            obj.verbLabel = verbsWithLabel
            obj.verbNoLabel = verbsNoLabel
            sentences.append(obj)
            sentence = []
            verbsWithLabel = []
            verbsNoLabel = []
    return sentences

def senseFormat(sense):
    if (sense is not None):
        sense = sense.name()
        sense = sense[:sense.index('.v')] + sense[sense.index('v.') + 1:]
    else:
        sense = "?"
    return sense

def findDative(sentence, where, when, who, tokenized=False):
    if tokenized:
        input = sentence
    else :
        input = nltk.word_tokenize(sentence)
        where = nltk.word_tokenize(where)
        when = nltk.word_tokenize(when)
        who = nltk.word_tokenize(who)
    tags = nltk.pos_tag(input)
    out = None
    for word, tag in tags:
        if (tag[:2] == 'NN' and word not in where and word not in when and word not in who):
            out = word
            break

    return out


def readFileCsv():
    sentences = []
    sentence = []
    for line in open('./testverbs.csv', 'r'):
        line = line.strip().lower()
        words = re.split(r'\t+', line)
        if (words[0] != None and words[0] != ''):  # with space as words[0] starts a new sentence
            sentence.append(words[1])
        else:
            sentences.append(sentence)
            sentence = []
    return sentences

def lesk(context_sentence, ambiguous_word, pos=None, synsets=None):

    context = set(context_sentence)
    if synsets is None:
        synsets = wn.synsets(ambiguous_word)

    if pos:
        synsets = [ss for ss in synsets if str(ss.pos()) == pos]

    if not synsets:
        return None

    _, sense = max(
        (len(context.intersection(ss.definition().split())), ss) for ss in synsets
    )

    return sense

def start(test=True):

    print('Let  us start !!!')
    if test:
        # for ss in wn.synsets('play'):
        #    print(ss, ss.definition())

        sentences = readFile()
        gold = []
        pred = []
        tot = 0
        corr = 0
        numCorr = 0;
        nonecount = 0
        for i in range(0, len(sentences)):
            for j in range(0, len(sentences[i].verbNoLabel)):
                ## PREDICATE IDENTIFICATION
                #NO NEED WITH CONLL

                ## PREDICATE DISAMBIGUATION
                sense = lesk(sentences[i].sentence, sentences[i].verbNoLabel[j], 'v')
                if (sense is not None):
                    sense = sense.name()
                    sense = sense[:sense.index('.v')] + sense[sense.index('v.') + 1:]
                pred.append(sense)
                gold.append(sentences[i].verbLabel[j])
                if (sense is None):
                    nonecount += 1
                elif (sense == sentences[i].verbLabel[j]):
                    corr += 1
                elif (sense[-2:] == sentences[i].verbLabel[j][-2:]):
                    numCorr += 1
                tot += 1
        print("Total verbs = {} \n"
              "Correct matches = {} \n"
              "Nones = {} \n"
              "Number of sens = {} \n"
              "Score {}%".format(tot, corr, nonecount, numCorr, corr / tot * 100))
    else:
        sentences = readFileCsv()
        predicted = []
        tot = 0
        nonecount = 0
        counter = 0;
        for i in range(0, len(sentences)):
            for pred in extractPred(sentences[i], True, counter):
                sense = lesk(sentences[i], pred.verb, 'v')
                if (sense is not None):
                    sense = sense.name()
                    sense = sense[:sense.index('.v')] + sense[sense.index('v.') + 1:]
                else :
                    nonecount += 1
                    sense = "?"
                tot += 1
                obj = Features()
                obj.sense = sense
                obj.index = pred.index
                predicted.append(obj)
            counter += len(sentences[i])
        writeOutput(predicted)
        print('**************')
        print("Total verbs = {} \n"
              "Nones = {} \n"
              "Score {}%".format(tot, nonecount, (1 - (nonecount / tot)) * 100))

def writeOutput(pred):
    with open('./1766342_test.txt', 'w') as out, open('./testverbs.csv', 'r') as input:
        counter = 0;
        lines = [line.rstrip('\n') for line in input]
        for line in lines:
            words = re.split(r'\t+', line)
            if (words[0] is None or words[0] == ''):  # with space as words[0] starts a new sentence
                out.write('\n')
                continue
            found = False
            for i in range(0, len(pred)):
                if (pred[i].index == counter):
                    out.write(line + '\t' + 'y' + '\t' + pred[i].sense + '\n')
                    found = True
                    break
            if (found == False):
                out.write(line + '\t_\t_\n')
            counter += 1;

def extractPred(sentence, tokenized=False, counter=0):
    if tokenized:
        input = sentence
    else :
        input = nltk.word_tokenize(sentence)
    tags = nltk.pos_tag(input)
    pred = []
    index = 0;
    for word, tag in tags:
        if (tag[:2] == 'VB' and notAuziliaryVerb(word) ):
            obj = Features()
            obj.verb = word
            obj.index= index + counter
            pred.append(obj)
        index +=1;

    return pred

def notAuziliaryVerb(verb):
    if (verb == 'be' or verb == 'am' or verb == 'was' or verb == 'are' or verb == 'were' or verb == 'is' or verb == 'have' or verb == 'has' or verb == 'had'):
        return False
    else: return True

def listSynset():
    syns = list(wn.all_synsets())
    offsets_list = [(s.offset(), s) for s in syns]
    offsets_dict = dict(offsets_list)
    return offsets_dict
