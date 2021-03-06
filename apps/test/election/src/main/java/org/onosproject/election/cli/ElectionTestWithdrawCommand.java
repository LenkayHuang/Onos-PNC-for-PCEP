/*
 * Copyright 2014-2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.election.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.cluster.LeadershipService;

/**
 * CLI command to withdraw the local node from leadership election for
 * the Election test application.
 */
@Command(scope = "onos", name = "election-test-withdraw",
        description = "Withdraw node from leadership election for the Election test application")
public class ElectionTestWithdrawCommand extends AbstractShellCommand {

    private static final String ELECTION_APP = "org.onosproject.election";

    @Override
    protected void execute() {
        LeadershipService service = get(LeadershipService.class);

        service.withdraw(ELECTION_APP);
        //print the current leader
        print("Withdrawing from leadership elections for the Election app.");
    }
}
