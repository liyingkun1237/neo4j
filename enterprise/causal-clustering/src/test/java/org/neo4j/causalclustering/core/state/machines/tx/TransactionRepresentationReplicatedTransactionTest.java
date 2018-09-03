/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.causalclustering.core.state.machines.tx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.neo4j.causalclustering.messaging.NetworkFlushableChannelNetty4;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.transaction.command.Command;
import org.neo4j.kernel.impl.transaction.log.PhysicalTransactionRepresentation;
import org.neo4j.storageengine.api.StorageCommand;

import static org.junit.Assert.assertEquals;

public class TransactionRepresentationReplicatedTransactionTest
{
    @Test
    public void shouldDecodeAndUnmarshalSameBytes() throws IOException
    {
        PhysicalTransactionRepresentation expectedTx =
                new PhysicalTransactionRepresentation( Collections.singleton( new Command.NodeCommand( new NodeRecord( 1 ), new NodeRecord( 2 ) ) ) );

        expectedTx.setHeader( new byte[0], 1, 2, 3, 4, 5, 6 );

        ByteBuf buffer = Unpooled.buffer();
        TransactionRepresentationReplicatedTransaction replicatedTransaction = ReplicatedTransaction.from( expectedTx );
        replicatedTransaction.marshal().marshal( new NetworkFlushableChannelNetty4( buffer ) );

        ReplicatedTransaction decoded = ReplicatedTransactionSerializer.decode( buffer );
        buffer.readerIndex( 0 );
        ReplicatedTransaction unmarshaled = ReplicatedTransactionSerializer.decode( buffer );

        assertEquals( decoded, unmarshaled );

        buffer.release();
    }

    private Collection<StorageCommand> ofSize( int size )
    {
        List<StorageCommand> commands = new ArrayList<>();
        for ( int i = 0; i < size; i++ )
        {
            commands.add( new Command.NodeCommand( new NodeRecord( 1 ), new NodeRecord( 2 ) ) );
        }
        return commands;
    }
}
