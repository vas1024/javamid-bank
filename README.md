


```mermaid
flowchart LR

%% ������������
    FRONT[Front UI<br>���-���������]
    ACCOUNTS[Accounts Service<br>���������� �������]
    CASH[Cash Service<br>������������� �����]
    TRANSFER[Transfer Service<br>�������� ����� �������]
    EXCHANGE[Exchange Service<br>����������� �����]
    EX_GEN[Exchange Generator<br>��������� ������ �����]
    BLOCKER[Blocker Service<br>���������� ��������]
    NOTIFY[Notifications Service<br>�����������]

%% ��������������
    FRONT -- "REST/JSON<br> GET /api/accounts" --> ACCOUNTS
    FRONT -- "REST/JSON<br>new client reg" --> ACCOUNTS
    FRONT --> CASH
    FRONT  --> TRANSFER
    FRONT  --> EXCHANGE
    ACCOUNTS  --> NOTIFY
    CASH  --> ACCOUNTS
    CASH  --> BLOCKER
    CASH  --> NOTIFY
    TRANSFER  --> ACCOUNTS
    TRANSFER  --> EXCHANGE
    TRANSFER  --> BLOCKER
    TRANSFER  --> NOTIFY
    EX_GEN  --> EXCHANGE

```

