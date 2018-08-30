import json
from predis.nltk_model import extractPred
from predis.nltk_model import lesk
from predis.nltk_model import senseFormat
from predis.nltk_model import findDative

class ArticleNews:
    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                          sort_keys=True, indent=4)
    pass

class Item:
    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                          sort_keys=True, indent=4)
    pass

class Document:
    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                          sort_keys=True, indent=4)
    pass

def findMax(who,what,where,when):
    l = len(who)
    if (len(what) > l):
        l = len(what)
    if (len(where) > l):
        l = len(where)
    if (len(when) > l):
        l = len(when)
    return l

if __name__ == '__main__':

    document = Document()
    document.what = [([('loves', 'VBZ'), ('Bob', 'NNP'), ('Marley', 'NNP'), ('in', 'IN'), ('Rome', 'NNP')], 4), ([('hates', 'VBZ'), ('Jhon', 'NNP'), ('in', 'IN'), ('Naples', 'NNP')], 2)]
    document.when = [[['Friday'], 16.0, 1529625600000.0]]
    document.where = [(['Bob', 'in', 'Rome'], 3), (['Naples'], 2)]
    document.who = [([('Alice', 'NNP')], 4), ([('Carl', 'NNP')], 2)]

    print(document.toJSON())
    print('*******')
    what = []
    where = []
    who = []
    when = []
    dative = []
    for elem in document.what:
        # YOU HAVE TO FIND DATIVE, UNDERSTAND DISAMBIGUATED
        item = Item()
        item.name = ''
        for el in elem[0]:
            item.name += el[0] + ' '
        item.score = elem[1]
        print('+++what')
        print(item.name, item.score)
        #print(elem)
        what.append(item)
    for elem in document.where:
        item = Item()
        item.name = ''
        for el in elem[0]:
            item.name += el + ' '
        item.score = elem[1]
        print('+++where')
        #print(elem)
        print(item.name, item.score)
        where.append(item)
    for elem in document.when:
        item = Item()
        item.name = ''
        for el in elem[0]:
            item.name += el + ' '
        item.score = elem[1]
        print('+++when')
        #print(elem)
        print(item.name, item.score)
        when.append(item)
    for elem in document.who:
        item = Item()
        item.name = ''
        for el in elem[0]:
            item.name += el[0] + ' '
        item.score = elem[1]
        print('+++who')
        #print(elem)
        print(item.name,item.score)
        who.append(item)

    news = []
    for i in range (0, findMax(who,what,where,when) + 10) :
        article = ArticleNews()
        #HWO
        for elem in who:
            if (elem.score == i):
                article.whoName = elem.name
                article.whoScore = 50
        #WHEN (FOR EVERY SENTENCE)
        article.whenName = ''
        article.whenScore = 0
        for elem in when:
            article.whenName += elem.name + ' - '
            article.whenScore += elem.score
        article.whenScore /= len(when)
        #WHERE
        for elem in where:
            if (elem.score == i):
                article.whereName = elem.name
                article.whereScore = 50
        # WHAT as last due to dative TODO
        for elem in what:
            if (elem.score == i):
                article.whatName = elem.name
                for pred in extractPred(article.whoName + elem.name):
                    sense = lesk(elem.name, pred.verb, 'v')
                    article.whatName = senseFormat(sense)
                article.whatScore = 50
                # NOW DATIVE
                article.dativeName = findDative(article.whoName + elem.name, article.whenName, article.whenName, article.whoName)
                article.dativeScore = 50

        if (hasattr(article, 'whoName') or hasattr(article, 'whatName')):
            news.append(article)

    #SHOW
    print('#########################\n#  NOW PRINT EXTRACTED  #\n#########################')
    print(json.dumps(news,default=lambda o: o.__dict__,sort_keys=True, indent=4))