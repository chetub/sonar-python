s = "Hello world."
s = "Hello \world." # Noncompliant {{Remove this "\", add another "\" to escape it, or make this a raw string.}}
#   ^^^^^^^^^^^^^^^
t = "Nice to \\ meet you"
t = "Nice to \ meet you" # Noncompliant
raw = r"Let's have \ lunch"
raw = R"Let's have \ lunch"
u = u"Let's have \ lunch" # Noncompliant
u = "Let's have \ lunch" # Noncompliant
v = 'Hello world.'
v = 'Hello \world.' # Noncompliant
w = """Hello \n world."""
w = """Hello \w world.""" # Noncompliant
w = " *\ " # Noncompliant
w = """*aa\ s""" # Noncompliant
inline_markup = """*a*\ s"""
inline_markup = """hello ```foo```\ s"""
inline_markup = """hello *foo*\ s"""
inline_markup = """hello **foo**\ s"""
inline_markup = """hello ref_\ s"""
inline_markup = """hello |sub|\ s"""
z = "abc\adef"
z = "abc\\\\aaa"
z = "*a*\ s" # Noncompliant
z = """\ s""" # Noncompliant
z = ""
re.compile(r'...'
           r'\("abc '
           'def"\)$') # Noncompliant

