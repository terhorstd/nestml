#
# LexerParserTest.py
#
# This file is part of NEST.
#
# Copyright (C) 2004 The NEST Initiative
#
# NEST is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# NEST is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with NEST.  If not, see <http://www.gnu.org/licenses/>.


import unittest
import os
from antlr4 import *
from pynestml.src.main.grammars.org.PyNESTMLLexer import PyNESTMLLexer
from pynestml.src.main.grammars.org.PyNESTMLParser import PyNESTMLParser


class LexerParserTest(unittest.TestCase):
    """
    This test is used to test the parser and lexer for correct functionality.
    """

    def test(self):
        for filename in os.listdir(
                os.path.realpath(os.path.join(os.path.dirname(__file__), os.path.join('..', '..', '..', '..',
                                                                                      'models')))):
            if filename.endswith(".nestml"):
                inputFile = FileStream(
                    os.path.join(os.path.dirname(__file__), os.path.join(os.path.join('..', '..', '..', '..',
                                                                                      'models'), filename)))
                # print('Start parsing ' + filename),
                lexer = PyNESTMLLexer(inputFile)
                # create a token stream
                stream = CommonTokenStream(lexer)
                # parse the file
                PyNESTMLParser(stream)
                # print(' ...done')
        return


if __name__ == '__main__':
    unittest.main()
