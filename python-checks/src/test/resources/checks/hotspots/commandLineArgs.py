import argparse
from argparse import ArgumentParser

import sys
from sys import argv
import mySys

from optparse import OptionParser
import optparse

def argparse_test():
    argparse.ArgumentParser() # Noncompliant {{Make sure that command line arguments are used safely here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^
    ArgumentParser() # Noncompliant
    argparse.otherFunction() # OK

def builtins():
    sys.argv # Noncompliant
#   ^^^^^^^^
    argv # Noncompliant
    mySys.argv # OK

def optparse_test():
    OptionParser() # Noncompliant
    optparse.OptionParser() # Noncompliant

def foo():
    return 1

foo()

from mypackage.myfile import innerfun

innerfun()
