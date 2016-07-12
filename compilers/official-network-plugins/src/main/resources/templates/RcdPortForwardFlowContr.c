/* ******** Start of template RcdPortForwardFlowContr.c */

//void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_send_ack(uint8_t seq){
bool /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_receive_msg_or_ack( uint8_t *rcv_buf_ptr, uint8_t rcv_len){
    bool forward_msg = false;

    uint8_t rx_seq_num = rcv_buf_ptr[0];
    if(rcv_len == 1) {
      // This is an ACK - Seq num is based on our TX
      if(rx_seq_num == /*PORT_NAME*/_instance.tx_seq_num) {
        //printf("RxAck rx(%d) == tx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.tx_seq_num);
        /*PORT_NAME*/_remove_oldest_from_tx_buf();  // This one is now ack'ed
        /*PORT_NAME*/_instance.tx_seq_num = ( /*PORT_NAME*/_instance.tx_seq_num + 1 ) % /*PORT_NAME*/_SEQ_MOD;
        /*PORT_NAME*/_send_oldest_in_tx_buf(); // Send out next if any pending
      } else {
        printf("RxAck rx(%d) != tx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.tx_seq_num);
        // TODO Error handling
      }
    } else {
      // This is a MSG - Seq num is chosen by remote
      //printf("RxData len(%d) seq(%d)\n", rcv_len, rx_seq_num);

      // Send ACK ... send_ack_RcdPortTunnel_2(rx_seq_num);
      byte tmp_buf[1];
      msgc_t   msg_out;      // Outgoing message
      if( Ports_ptr->IsConnected(1) ) {
        //printf("Send ack(%d)\n", seq); //Crash maker
        tmp_buf[0] = rx_seq_num;
        APP_MSGC_comp_MSGID_TUNNEL(&msg_out, tmp_buf, 1);
        Ports_ptr->SendMsgc(1, &msg_out);
      }


      if((rx_seq_num == 0) || (rx_seq_num == /*PORT_NAME*/_instance.rx_seq_num + 1)) {
        
        /*PORT_NAME*/_instance.rx_seq_num = rx_seq_num;
        forward_msg = true;
        //externalMessageEnqueue( &(rcv_buf_ptr[1]), rcv_len-1, /*PORT_NAME*/_instance.listener_id);
      } else {
        printf("RxData rx(%d) != expected_rx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.rx_seq_num + 1);
      }
    }




    byte tmp_buf[1];
    msgc_t   msg_out;      // Outgoing message
    if( Ports_ptr->IsConnected(1) ) {
        //printf("Send ack(%d)\n", seq); //Crash maker
        tmp_buf[0] = seq;
        APP_MSGC_comp_MSGID_TUNNEL(&msg_out, tmp_buf, 1);
        Ports_ptr->SendMsgc(1, &msg_out);
    }
}

void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_timeout_check(void){
    uint32_t timeout_ticks = HOST_MS2TICK(/*PORT_NAME*/_TIMEOUT_MS);
    if (timeout_ticks == 0) timeout_ticks = 1;
        
    timeout_ticks = /*PORT_NAME*/_instance.last_sent_tick + timeout_ticks;
    if ( HOST_TICK_NOW() > timeout_ticks ) {
        // Try to resend to retry ack
        printf("Try timeout\n");
        /*PORT_NAME*/_send_oldest_in_tx_buf();
    }
}

uint8_t /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_remove_oldest_from_tx_buf(void){
    uint16_t rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    uint8_t buf_len = /*PORT_NAME*/_instance.tx_buf[rd_idx];
    int i;
    
    if (/*PORT_NAME*/_instance.tx_buf_rd_idx != /*PORT_NAME*/_instance.tx_buf_wr_idx) {
        rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        for (i = 0; i < buf_len; i++) {
            rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        }
        /*PORT_NAME*/_instance.tx_buf_rd_idx = rd_idx;
    }
    return rd_idx;
}

void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_make_room_in_tx_buf( uint8_t req_len){
    bool room_in_tx_buf;
    uint16_t rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    uint16_t wr_idx = /*PORT_NAME*/_instance.tx_buf_wr_idx;
    
    while(1) {
        room_in_tx_buf = true;
        
        // Check if it is room
        if(wr_idx > rd_idx) {
            if(((wr_idx + req_len) >= /*PORT_NAME*/_TX_BUF_SIZE) &&
               (((wr_idx + req_len) - /*PORT_NAME*/_TX_BUF_SIZE) >= rd_idx)) {
                room_in_tx_buf = false;        // No room
            }
        }
        else if(wr_idx < rd_idx) {
            if((wr_idx + req_len) >= rd_idx){
                room_in_tx_buf = false;        // No room
            }
        }
        
        if( room_in_tx_buf ) break; // It is room ... skip the loop
        
        // It is not room, remove oldest from buf
        rd_idx = /*PORT_NAME*/_remove_oldest_from_tx_buf();

        printf("Buffer overflow oldest message skipped\n");
    } 
  
}

void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_put_into_tx_buf( uint8_t *buf_ptr, uint8_t buf_len){
    uint16_t wr_idx = /*PORT_NAME*/_instance.tx_buf_wr_idx;
    uint16_t rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    uint8_t *tx_buf_ptr =  /*PORT_NAME*/_instance.tx_buf;
    uint8_t req_len;
    bool empty_buf = rd_idx == wr_idx;
    
    if( buf_len <= /*PORT_NAME*/_MTU_SIZE ) {
        
        req_len = buf_len + 1; // Request one extra byte to store buf_len
        
        /*PORT_NAME*/_make_room_in_tx_buf(req_len);

        //printf("Put into tx buf len(%d) (%d)\n", buf_len, empty_buf);
        tx_buf_ptr[wr_idx] = buf_len;
        wr_idx = (wr_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        for (int i = 0; i < buf_len; i++) {
            tx_buf_ptr[wr_idx] = buf_ptr[i];
            wr_idx = (wr_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        }
        /*PORT_NAME*/_instance.tx_buf_wr_idx = wr_idx;
    } else {
        // TODO do fragmentation here
        printf("Error len(%d) > MTU(%d) ... fragmentation not supported in this setup\n", buf_len, /*PORT_NAME*/_MTU_SIZE);
    }
    
    if ( empty_buf ) {
        /*PORT_NAME*/_send_oldest_in_tx_buf();
    } else {
        /*PORT_NAME*/_timeout_check();
    }
}

void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_send_oldest_in_tx_buf(void){
    byte tmp_buf[/*PORT_NAME*/_MTU_SIZE];
    msgc_t   msg_out;      // Outgoing message
    if( /*PORT_NAME*/_instance.tx_buf_rd_idx != /*PORT_NAME*/_instance.tx_buf_wr_idx){
        if( Ports_ptr->IsConnected(1) ) {
            uint16_t rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
            uint8_t *tx_buf_ptr =  /*PORT_NAME*/_instance.tx_buf;
            uint8_t buf_len;
            
            tmp_buf[0] = /*PORT_NAME*/_instance.tx_seq_num;

            buf_len = tx_buf_ptr[rd_idx];
            rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
            for (int i = 0; i < buf_len; i++) {
                tmp_buf[i+1] = tx_buf_ptr[rd_idx];
                rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
            }
            
            //printf("Send oldest len(%d) seq(%d)\n", buf_len, /*PORT_NAME*/_instance.tx_seq_num);

            APP_MSGC_comp_MSGID_TUNNEL(&msg_out, tmp_buf, buf_len+1);
            Ports_ptr->SendMsgc(1, &msg_out);
            /*PORT_NAME*/_instance.last_sent_tick = HOST_TICK_NOW();
        }
    }
}


void /*CFG_CPPNAME_SCOPE*//*PORT_NAME*/_set_listener_id(uint16_t id) {
	/*PORT_NAME*/_instance.listener_id = id;
}
/* ******** End of template RcdPortForwardFlowContr.c */
