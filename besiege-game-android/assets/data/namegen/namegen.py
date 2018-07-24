#-*- coding: utf-8 -*-
import random

# from http://www.geocities.com/anvrill/names/cc_goth.html
PLACES = ['Adara', 'Adena', 'Adrianne', 'Alarice', 'Alvita', 'Amara', 'Ambika', 'Antonia', 'Araceli', 'Balandria', 'Basha',
'Beryl', 'Bryn', 'Callia', 'Caryssa', 'Cassandra', 'Casondrah', 'Chatha', 'Ciara', 'Cynara', 'Cytheria', 'Dabria', 'Darcei',
'Deandra', 'Deirdre', 'Delores', 'Desdomna', 'Devi', 'Dominique', 'Drucilla', 'Duvessa', 'Ebony', 'Fantine', 'Fuscienne',
'Gabi', 'Gallia', 'Hanna', 'Hedda', 'Jerica', 'Jetta', 'Joby', 'Kacila', 'Kagami', 'Kala', 'Kallie', 'Keelia', 'Kerry',
'Kerry-Ann', 'Kimberly', 'Killian', 'Kory', 'Lilith', 'Lucretia', 'Lysha', 'Mercedes', 'Mia', 'Maura', 'Perdita', 'Quella',
'Riona', 'Safiya', 'Salina', 'Severin', 'Sidonia', 'Sirena', 'Solita', 'Tempest', 'Thea', 'Treva', 'Trista', 'Vala', 'Winta']

CITIES = ["Catterick",
			"Colne",
			"Fotheringhay",
			"Hawarden",
			"Gloucester",
			"Furness",
			"Weobley",
			"Billinghame",
			"Portishead",
			"Oakham",
			"Warkworth",
			"Kidwelly",
			"Brading",
			"Hayton",
			"Neville",
			"Portsmouth",
			"Colchester",
			"Newstead",
			"Sheffield",
			"Salisbury",
			"Knaresborough",
			"Norham",
			"Brackley",
			"Elstow",
			"Bloxham",
			"Rhuddlan",
			"Dudley",
			"Scunthorpe",
			"Hexham",
			"Usk",
			"Bebington",
			"Wilton",
			"Minster",
			"Walpole",
			"Lewes",
			"Bamburgh",
			"Buildwas",
			"Pebmarsh",
			"Oxford",
			"Oxted"]

KHMER = ["Battambang",
"Kampong Cham",
"Kampong Chhnang",
"Kampong Som",
"Kampong Speu",
"Kampong Thom",
"Kampot",
"Koh Kong",
"Kratié",
"Mongkol Borei",
"Neak Leung",
"Pailin",
"Phnom Penh",
"Poipet",
"Preah Net Preah",
"Samraong",
"Takéo",
"Prey Veng",
"Pursat",
"Siem Reap",
"Sisophon",
"Stung Treng",
"Svay Rieng",
"Ta Khmau",
"Thmar Kol",
"Thmar Puok",]
			
###############################################################################
# Markov Name model
# A random name generator, by Peter Corbett
# http://www.pick.ucam.org/~ptc24/mchain.html
# This script is hereby entered into the public domain
###############################################################################
class Mdict:
    def __init__(self):
        self.d = {}
    def __getitem__(self, key):
        if key in self.d:
            return self.d[key]
        else:
            raise KeyError(key)
    def add_key(self, prefix, suffix):
        if prefix in self.d:
            self.d[prefix].append(suffix)
        else:
            self.d[prefix] = [suffix]
    def get_suffix(self,prefix):
        l = self[prefix]
        return random.choice(l)  

class MName:
    """
    A name from a Markov chain
    """
    def __init__(self, chainlen = 2):
        """
        Building the dictionary
        """
        if chainlen > 10 or chainlen < 1:
            print "Chain length must be between 1 and 10, inclusive"
            sys.exit(0)
    
        self.mcd = Mdict()
        oldnames = []
        self.chainlen = chainlen
    
        for l in KHMER:
            l = l.strip()
            oldnames.append(l)
            s = " " * chainlen + l
            for n in range(0,len(l)):
                self.mcd.add_key(s[n:n+chainlen], s[n+chainlen])
            self.mcd.add_key(s[len(l):len(l)+chainlen], "\n")
    
    def New(self):
        """
        New name from the Markov chain
        """
        prefix = " " * self.chainlen
        name = ""
        suffix = ""
        while True:
            suffix = self.mcd.get_suffix(prefix)
            if suffix == "\n" and len(name) < 4:
				continue
            if suffix == "\n" or len(name) > 9:
                break
            else:
                name = name + suffix
                prefix = prefix[1:] + suffix
        return name.capitalize()  

allNames = set()
for i in range(100):
    allNames.add(MName().New())
	
for name in allNames:
	print name
print len(allNames)
	
	
	
	