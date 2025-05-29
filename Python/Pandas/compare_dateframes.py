'''

Compare two Pandas Dataframes
@params
keys -- a list of column names, that forms as a common key of the two dataframes to identify each row, such that there is no
    duplicated keys in each dataframe
compare_cols -- a list of column names need to be compared

'''
import os, sys
import pandas as pd
import logging

logging.basicConfig(format='%(asctime)s %(module)s - %(message)s',
                    stream=sys.stdout,
                    level=logging.DEBUG,
                    datefmt='%Y-%m-%d %H:%M:%S')
log = logging.getLogger(__name__)


class DataframeCompare():

    #
    # return a dataframe with an additional column 'DIFF', which is a list of tuples,
    # each tuple is (column_name, src value, tgt value, MISMATCH) for each row; is None if there is no differences
    #
    def compare_df(self, src_df, tgt_df, keys, compare_cols, keep_match=False):

        if len(src_df[src_df[keys].duplicated()]) > 0:
            log.info('Duplicated keys in source')
            return None

        if len(tgt_df[tgt_df[keys].duplicated()]) > 0:
            log.info('Duplicated keys in target ')
            return None

        merge_df = src_df.merge(tgt_df, on=keys, how='outer', suffixes=('_s', '_t'))

        src_cols = [f'{s}_s' for s in compare_cols]
        tgt_cols = [f'{s}_t' for s in compare_cols]

        def diff_func(row):
            if keep_match:
                diff_list = []
                for idx, (i, j) in enumerate(zip(row[src_cols], row[tgt_cols])):
                    diff_list.append((src_cols[idx][:-2], i, j, 'MATCH')) if i == j else diff_list.append(
                        (src_cols[idx][:-2], i, j, 'MISMATCH'))
                return diff_list
            else:
                difference = [(src_cols[idx][:-2], i, j, 'MISMATCH') for idx, (i, j) in
                              enumerate(zip(row[src_cols], row[tgt_cols])) if
                              i != j]

                return pd.NA if len(difference) == 0 else difference

        log.info('Finding differences ...')
        merge_df = merge_df.fillna('')
        merge_df['DIFF'] = merge_df.apply(diff_func, axis=1)
        log.info('Finished finding differences')

        return merge_df

    #
    # process the diff dataframe from method compare_df() to generate final report,
    # can add additional methods to generate different report.
    #
    def process_diff(self, diff_df, keys, keep_match=False):

        if not keep_match:
            diff_df = diff_df[pd.isna(diff_df.DIFF) == False]

        if diff_df is None:
            log.info('  - Diff result is None, skip processing ...')
            return None

        if len(diff_df) == 0:
            log.info(f"    - No difference found, skip processing ...")
            return None

        diff_df = diff_df[keys + ['DIFF']]
        diff_df = diff_df.explode('DIFF')
        diff_df['COLUMN_NAME'], diff_df['SRC_VALUE'], diff_df['TGT_VALUE'], diff_df['STATUS'] = zip(*diff_df.DIFF)

        return diff_df[list(diff_df.columns.drop('DIFF'))]


def main():
    dataframeCompare = DataframeCompare()
    df1 = pd.DataFrame({
        'Name': ['Tom', 'Nick', 'Krish', 'Jack'],
        'Age': [20, 21, 19, 18]
    })
    df2 = pd.DataFrame({
        'Name': ['Tom', 'Nick', 'Krish', 'Jack'],
        'Age': [20, 21, 22, 23]
    })
    keys = ['Name']
    compare_cols = ['Age']
    diff = dataframeCompare.compare_df(df1, df2, keys=keys, compare_cols=compare_cols)
    df = dataframeCompare.process_diff(diff, keys=keys)
    log.info(df)


if __name__ == '__main__':
    main()

