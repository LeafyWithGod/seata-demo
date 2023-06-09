package cn.itcast.account.service.impl;

import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountTCCService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccuntTCCServiceImpl implements AccountTCCService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountFreezeMapper accountFreezeMapper;


    @Override
    @Transactional
    public void deduct(String userId, int money) {
        //0.获取事务id
        String xid = RootContext.getXID();
        //1.扣减余额
        accountMapper.deduct(userId, money);
        //2.记录冻结金额，事务状态
        AccountFreeze freeze = new AccountFreeze();
        freeze.setUserId(userId);
        freeze.setState(AccountFreeze.State.TRY);
        freeze.setXid(xid);
        accountFreezeMapper.insert(freeze);
    }

    @Override
    public boolean confirm(BusinessActionContext ctx) {
        //获取事务id，然后删除冻结记录
        String xid = ctx.getXid();
        int i = accountFreezeMapper.deleteById(xid);
        return i == 1;
    }

    @Override
    public boolean cancel(BusinessActionContext ctx) {
        //恢复可用余额，清除冻结金额
        accountMapper.refund();
        return false;
    }
}
