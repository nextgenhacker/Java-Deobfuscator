/*
A(n incomplete) parser for decompiled java bytecode.  Note that this ignores expressions!
*/
package parser

import (
	"io"
)

type JAST struct {
	classes []JClass
}

type JClass struct {
	id         string
	super      *JClass
	attributes []JAttribute
	methods    []JMethod
}

type JMethod struct {
	id string
	ret string
	parameters []JAttribute
	instr []string
}

type JAttribute struct {
	id string
	t  JType
}

type JType string

func Parse(input io.Reader) JAST {
	//FIXME
	return JAST{}
}
