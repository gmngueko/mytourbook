<?php
/*******************************************************************************
 * Copyright (c) 2007-2009 Intalio, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antoine Toulme, Intalio Inc.
*******************************************************************************/

define('BABEL_BASE_DIR', "../../");

require("../spec_helper.php");
require (BABEL_BASE_DIR . "classes/system/file.class.php");

class DescribeFileClass extends PHPSpec_Context {

	public function before() {
    }

    public function itShouldAppendTheLangCode() {
	  $this->spec(File->appendLangCode('en_AA', 'blah.properties'))->should->equal('blah_en_AA.properties');
    }
}

?>